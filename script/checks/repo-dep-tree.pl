#!/usr/bin/perl

# read in list of repositories (in order)

# for each repo
  # get list of dependencies in repo, save in hashmap

# for each repo
  # for each pom.xml in repo
    # scan pom.xml for deps
    # for each dep found
      # check if dep is in repo-deps
      # if yes, 
        # add relationship: this-repo -> dep-repo (list: dep)

use Getopt::Std;
use Cwd             qw(abs_path);
use File::Basename  qw( dirname);
use File::Find;
use XML::Simple;
use Data::Dumper;

getopts('hdfwvt:');

my $print_deps = 1;
if( $opt_t ) { 
  --$print_deps;
}

my $verbose = 0;
if( $opt_v ) { 
  ++$verbose;
}

my $filter_transitive = 0;
if( $opt_f ) { 
  ++$filter_transitive;
}

my $create_dot_file = 0;
if( $opt_d ) { 
  ++$create_dot_file;
}

if( $opt_h ) { 
  print "\n";
  print " h ........ print [h]elp (this)\n";
  print " d ........ create [d]ot file (implies -f)\n";
  print " f ........ [f]ilter transitive dependencies (not compatible with -t)\n";
  print " w ........ [w]arn instead of dying on invalid module versions\n";
  print " v ........ [v]erbose: show which modules are referenced\n";
  print " t <repo> . print the repositories that should be built before the [t]arget repository\n";
  die "\n";
}

my $warn = 0;
if( $opt_w ) { 
  ++$warn;
}

# variables

my $repo_file = "../repository-list.txt";
my (%repo_mods, %repo_deps, %mod_repos, %repo_tree, %repo_sorted);
my (@repo_list, @repo_sorted);
my ($repo, $dep, $branch_version);
my ($module, $xml, $data);
my @build_chain; # required repositories are stored here by build() subroutine
my %blocked = {}; # helper for resolving build chain

# subs

sub collectModules {
  unless( /^pom.xml$/ ) {
    return;
  }
  my $dir = $File::Find::dir;

  my $this_repo = $dir;
  $this_repo =~ s#/.*##;

  my $file = $File::Find::name;

  $xml = new XML::Simple;
  $data = $xml->XMLin($_);
  $module = getModule($data, $this_repo);

  # collect repo module info
  if( ! exists $repo_mods{$this_repo} ) { 
    $repo_mods{$this_repo} = {};
  }
  $repo_mods{$this_repo}->{$module} = 1;

  # collect repo dependency info
  my $dep_arr_ref = $data->{'dependencies'}->{'dependency'};
  if( ! defined $dep_arr_ref ) { 
    return;
  } elsif( $dep_arr_ref =~ /^HASH/ ) { 
    my $dep_id = "$dep_arr_ref->{'groupId'}:$dep_arr_ref->{'artifactId'}";
    if( ! exists $repo_deps{$dep_id} ) { 
      $repo_deps{$dep_id} = {};
    }
    $repo_deps{$dep_id}{$this_repo} = 1;
    return;
  } else { 
    foreach my $dep (@{$dep_arr_ref}) { 
      my $dep_id = "$dep->{'groupId'}:$dep->{'artifactId'}";
      if( ! exists $repo_deps{$dep_id} ) { 
        $repo_deps{$dep_id} = {};
      }
      $repo_deps{$dep_id}{$this_repo} = 1;
    }
  }
 
}

sub getModule() {
  my $xml_data = shift();
  my $repo = shift();

  my $groupId = $xml_data->{'groupId'};
  if( $groupId eq "" ) { 
    $groupId = $xml_data->{'parent'}->{'groupId'};
  }
  my $module = "$groupId:$xml_data->{'artifactId'}";

  # check version 
  my $version = $xml_data->{'version'};
  if( $version eq "" ) { 
    $version = $xml_data->{'parent'}->{'version'};
  }
  if( ! defined $branch_version ) { 
    $branch_version = $version;
  } elsif( $groupId !~ /^org.uberfire/ ) { 
    if( $branch_version ne $version ) { 
      $msg = "Incorrect version ($version) for $module in $repo\n";
      if( $warn ) { 
        print $msg;
      } else {
        die "$msg";
      }
    }
  }

  return $module;
}

sub onlyLookAtPoms { 
  my @pom_files = grep { $_ =~ /pom.xml/ } @_;
  my @dirs = grep { -d $_ } @_;
  my @filesToProcess = ();

  foreach my $fileName ( "src", "target", "bin", "resources", "kie-eap-modules", "META-INF" ) { 
    @dirs = grep { ! ( $_ eq $fileName && -d $_ ) } @dirs;
  }

  @filesToProcess = (@pom_files, @dirs );

  return @filesToProcess;
}

sub build {
  my $target = shift();
  my $depth = shift();
  $blocked{$target} = 0;
  if( exists $repo_tree{$target} ) {
    # traverse the graph recursively
    foreach my $child ( sort keys %{$repo_tree{$target}} ) {
      if( scalar @{ ${repo_tree}{$target}{$child} } > 0 ) { # is direct dependency
        if( $verbose ) {
          printf( "%s%s -> %s\n", "- " x $depth, $target, $child );
        }
        if ( exists ${blocked}{$child} && ${blocked}{$child} == 0 ) {
          # is already built
          next;
        }
        ++$blocked{$target};
        if( ${blocked}{$child} > 0 ) {
          if( $verbose ) {
            print "CYCLE DETECTED!\n";
          }
          next;
        }
        build( $child, $depth + 1 );
        if( ${blocked}{$child} == 0 ) {
          --$blocked{$target};
        }
      }
    }
  }

  if ( $blocked{$target} == 0 ) {
    # either no deps or all direct deps built => append this target to build chain
    push( @build_chain, $target);
    if( $verbose ) { print "Building: $target\n"; }
  } else {
    print "Blocked repo: $target\n";
  }
}

sub filterTransitiveDependencies { 
  # Remove shortcut dependencies, 
  # i.e. remove each dependency A->C if path A->B->C exists.
  
  foreach $dep_repo (keys %repo_tree) {
    foreach my $src_repo (keys %{$repo_tree{$dep_repo}} ) {
      foreach my $inter_repo (keys %{$repo_tree{$dep_repo}} ) {
        if( defined $repo_tree{$inter_repo}{$src_repo} ) {
          # do not delete the link entirely, it helps to detect 2+ step shortcuts
          $repo_tree{$dep_repo}{$src_repo} = [];
        }
      }
    }
  }
}

sub show { 
  if( $print_deps ) { 
    print shift();
  }
}

# main

open(LIST, "<$repo_file" ) 
  || die "Unable to open $repo_file: $!\n";
while(<LIST>) { 
  chomp($_);
  push( @repo_list, $_ );
}
push( @repo_list, "uberfire" );

my $script_home_dir = dirname(abs_path($0));
chdir "$script_home_dir/../../../";
my $root_dir = Cwd::getcwd();

my $repo;
for my $i (0 .. $#repo_list ) { 
  $repo = $repo_list[$i]; 

  if( ! -d $repo ) { 
    die "Could not find directory for repository '$repo' at $root_dir!\n";
  } 

  find( {
    wanted => \&collectModules, 
    preprocess => \&onlyLookAtPoms
    }, $repo);
}

print "- Finished collecting module information.\n";

foreach $repo (keys %repo_mods) { 
  foreach $dep (keys %{$repo_mods{$repo}}) { 
    if( exists $mod_repos{$dep} ) { 
      print "The $dep module exists in both the $mod_repos{$dep} AND $repo repositories!\n";
    } else { 
      $mod_repos{$dep} = $repo;
    }
  }
}

print "- Finished ordering module information.\n";

# repo_deps : dependency -> repository in which the dependency is used (dependent)
# mod_repos : module -> repository in which the module is located (source) 
foreach $dep ( keys %repo_deps ) {
  my $src_repo = $mod_repos{$dep};
  if( $src_repo ) { # otherwise it is a 3rd party artifact
    foreach my $dep_repo ( keys %{$repo_deps{$dep}} ) {
      if( $src_repo eq $dep_repo ) {
        # dependencies inside a repository are OK
        next;
      }
      if( ! exists $repo_tree{$dep_repo} ) { 
        $repo_tree{$dep_repo} = {};
      } 
      if( ! defined $repo_tree{$dep_repo}{$src_repo} ) { 
        $repo_tree{$dep_repo}{$src_repo} = [];
      } 
      $dep =~ s/^[^:]*://;
      push( @{ $repo_tree{$dep_repo}{$src_repo} }, $dep );
    }
  }
}

print "- Finished creating repository dependency tree.\n";

my %build_tree;

if( $filter_transitive ) { 
  filterTransitiveDependencies();
}

show( "\nDependent-on tree: \n" );
foreach $repo (sort @repo_list) {
  show( "\n$repo (is dependent on): \n" );
  foreach my $leaf_repo (sort keys %{$repo_tree{$repo}} ) {
    @deps = @{ $repo_tree{$repo}{$leaf_repo} };
    if( scalar @deps > 0 ) {
      $deps_str = $verbose ? join( ',', @deps ) : scalar @deps;
      show( "- $leaf_repo ($deps_str)\n" );
    }
    if( ! exists $build_tree{$leaf_repo} ) { 
      $build_tree{$leaf_repo} = {};
    }
    ++$build_tree{$leaf_repo}{$repo};
  }
}

show( "\nDependencies tree: \n" );
foreach $repo (sort keys %build_tree) {
  show( "\n$repo (is used by): \n" );
  foreach my $leaf_repo (sort keys %{$build_tree{$repo}} ) {
    show( "- $leaf_repo\n" );
  }
}


if( $create_dot_file ) { 
  if( ! $filter_transitive ) { 
    filterTransitiveDependencies();
  }
  # Transform the build graph into DOT language.
  my $dot = "digraph {\n";
  foreach $repo (keys %repo_tree) {
    $dot .= sprintf("  %s;\n", $repo =~ s/-/_/gr);
    foreach my $leaf_repo (keys %{$repo_tree{$repo}}) {
      if ( scalar @{ $repo_tree{$repo}{$leaf_repo} } > 0) {
        $dot .= sprintf("  %s -> %s;\n", $repo =~ s/-/_/gr, $leaf_repo =~ s/-/_/gr, $style);
      }
    }
    $dot .= "\n";
  }
  $dot .= "}\n";
  
  # Write it to a file. The graph image can be produced simply by running
  # $ dot -O -Tpng dep-tree.dot
  my $filename = "dep-tree.dot";
  open(my $dotfile, ">", $filename) or die "Can't open dep-tree.dot: $!";
  print $dotfile $dot;
  close $dotfile or die "$dotfile: $!";
  printf "\nGraph written to '%s'.\n", abs_path( $filename );
  print "Run 'dot -O -Tpng $filename' to render PNG image.\n";
}

# Print the list of repositories required to be built before building target repository.
if ( $opt_t ) {
  build( $opt_t, 0 );

  if( $blocked{$opt_t} > 0 ) {
    print "\nRepository '$opt_t' cannot be built in a non-snapshot version due to circular dependencies!\n";
    if( ! $verbose ) { print "Re-run in verbose mode to see the cause.\n"; }
  }
  print "\nYou need to build following repositories before building '$opt_t' (in order):\n";
  print join( ',', @build_chain ), "\n";
}

