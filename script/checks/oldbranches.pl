#!/usr/bin/perl

use DateTime;
use Cwd qw(abs_path);

use POSIX;
use strict;
use warnings;

my $home = abs_path($0);
$home =~ s#/[\w-]+/[\w-]+/[\w-]+/[\w+\.]+$##;

my $git = "/usr/bin/git";
if( ! -e $git ) { 
  die "'$git' does not exist on this system! Modify the $git variable before running this script!\n";
}

my $daysAfterWhichBranchIsTooOld = 180;

my $tz = strftime("%z", localtime());
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900; ++$mon; 
if( $mon < 10 ) { 
  $mon = "0" . $mon;
}
if( $mday < 10 ) { 
  $mday = "0" . $mday;
}
my $now = DateTime->new(
   year      => $year,
   month     => ($mon/1),
   day       => ($mday/1),
   time_zone => 'local',
);

my %source;
while(<DATA>) { 
  chomp;
  my $repo;
  if( /^#/ ) { 
    next;
  }
  if( /^([^:]+):(.*)/) { 
    $repo = $1;
    $source{$repo} = $2;
  }
  chdir "$home/$repo"
    || die "Unable to change directory to [$home/$repo]: $!\n";

  print "\n";
  my @branches = &gbrh($source{$repo}, $repo);
  print "\n";
  print "$repo\n";
  print ("=" x length($repo));
  print "\n";

  my @list;
  my $branch;
  foreach $branch (@branches) { 
    my $cmd = "$git show -s --format=\"%ci [%cn]\" remotes/$source{$repo}/$branch";
    open( INFO, "$cmd |" ) 
      || die "Unable to execute [$cmd]: $!\n";
    while(<INFO>) { 
      chomp;
      push @list, "$_ $branch";
    }
  }
  my @sorted = sort @list;
  foreach $branch (sort @list) { 
    if( $branch =~ /^(\d+)-(\d+)-(\d+) \d+:\d+:\d+\s+([-+]\d+)\s+\[([^\]]+)\]\s+(\S+)/ ) { 
      my $y = $1;
      my $m = $2;
      my $d = $3;
      my $tz = $4;
      my $who = $5;
      my $br = $6;
      my $then = DateTime->new(
        year      => $y,
        month     => $m,
        day       => $d,
        time_zone => 'local'
      );
      my $days = $then->delta_days($now)->in_units('days');
      my $out = sprintf( "%s-%s-%s - %-25s %s\n", $y, $m, $d, $who, $br );
      if( $days >= $daysAfterWhichBranchIsTooOld ) { 
        print $out;
      } else { 
        print "* too recent: $out";
      }
    } else {
      die "Unexpected format: [$branch]\n";
    }
  }
}
print "\n";

## subroutines

sub gbrh() { 
  my $source = $_[0];
  my $branch = $_[1];
  # update
  call("$git remote update $source -p");
  # get branches
  my $cmd = "$git branch -a";
  open( BRANCHES, "$cmd |" )
    || die "Unable to execute [$cmd]: $!\n";
  my @branches;
  print "$branch excludes:\n";
  while(<BRANCHES>) { 
    chomp;
    s/^..([^ ]+).*/$1/;
    if( s#remotes/$source/## ) { 
      if( m/r?[567]\.\d\.(\d\.((M\d|Beta\d|GA)\.)?)?(x|Final)|HEAD|master$/ ) { 
        print "  $_\n";
      } else {
        push(@branches, $_);
      }
    }
  }
  return @branches;
}

__DATA__
drools:origin
jbpm:origin
droolsjbpm-knowledge:upstream
droolsjbpm-build-bootstrap:upstream
droolsjbpm-integration:upstream
guvnor:upstream
kie-wb-distributions:upstream
