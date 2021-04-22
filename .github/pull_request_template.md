Many thanks for submitting your Pull Request :heart:! 

Please make sure that your PR meets the following requirements:

- [ ] You have read the [contributors guide](CONTRIBUTING.md)
- [ ] Your code is properly formatted according to [this configuration](https://github.com/kiegroup/kogito-runtimes/tree/master/kogito-ide-config)
- [ ] Pull Request title is properly formatted: `KOGITO-XYZ Subject`
- [ ] Pull Request title contains the target branch if not targeting master: `[0.9.x] KOGITO-XYZ Subject`
- [ ] Pull Request contains link to the JIRA issue
- [ ] Pull Request contains link to any dependent or related Pull Request
- [ ] Pull Request contains description of the issue
- [ ] Pull Request does not include fixes for issues other than the main ticket

<details>
<summary>
How to retest this PR or trigger a specific build:
</summary>

* <b>Pull Request</b>  
  Please add comment: <b>Jenkins retest this</b>
 
* <b>Quarkus LTS checks</b>  
  Please add comment: <b>Jenkins run LTS</b>

* <b>Native checks</b>  
  Please add comment: <b>Jenkins run native</b>

* <b>Full Kogito testing</b> (with cloud images and operator BDD testing)  
  Please add comment: <b>Jenkins run BDD</b>  
  <b>This check should be used only if a big change is done as it takes time to run, need resources and one full BDD tests check can be done at a time ...</b>
</details>

<details>
<summary>
How to use multijob PR check:
</summary>

The multijob PR check is running different jobs for the current repository and each downstream repository, one after the other (or parallel)
with the following dependency graph:

           runtimes
              |
          optaplanner
              |
            -----
            |    |
          apps   examples

* <b>Run (or rerun) all tests</b>  
  Please add comment: <b>Jenkins (re)run multijob tests</b>
 
* <b>Run (or rerun) dependent test(s)</b>  
  Please add comment: <b>Jenkins (re)run multijob [optaplanner|apps|examples] tests</b>

* <b>Run (or rerun) all LTS tests</b>  
  Please add comment: <b>Jenkins (re)run multijob LTS</b>
 
* <b>Run (or rerun) LTS dependent test(s)</b>  
  Please add comment: <b>Jenkins (re)run multijob [optaplanner|apps|examples] LTS</b>

* <b>Run (or rerun) all native tests</b>  
  Please add comment: <b>Jenkins (re)run multijob native</b>
 
* <b>Run (or rerun) native dependent test(s)</b>  
  Please add comment: <b>Jenkins (re)run multijob [optaplanner|apps|examples] native</b>

*NOTE: Running a dependent test will run also following dependent projects.*
</details>