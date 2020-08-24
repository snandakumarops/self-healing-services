# Self healing consumer services
All of the services use the hostname as the key for the kafk topics.
## Check Event Service
For every incoming sensu failure event, this service aggregates the historical ansible tower events for the same host, and checks to see if the remediation playbook needs to be invoked. The decisioning logic for this component is defined using business rules as defined as a Decision Model  Notation(DMN). The decision results are written on a topic.
This service uses the camel aggregator pattern for collecting past events for a host. This for now uses an in-memory aggregator (hashmap). Clean up logic will have to be specified for a production  usecase.
```
cd check-event-service
mvn fabric8:deploy
cd ..
```
## Invoke Ansible Service
For ansible events that needs remediation as determined by the check event service, this service invokes the ansible tower REST APIs to invoke the corresponding job template. The response which is a job id for the job invocation is added on to a new topic.
The ansible tower url is configurable. Make sure to change the url and the bearer token accordingly.
```
cd invoke-ansible
mvn fabric8:deploy
cd ..
```
## Check Ansible Status Service
This service checks the status of the ansible job template run, and updates the results on topic. Since, each playbook has different runtimes, a run which is still in progress is reprocessed on this stream until the job completes.
The ansible tower url is configurable. Make sure to change the url and the bearer token accordingly.
```
cd ansible-status
mvn fabric8:deploy
cd  ..
```
## Create Incident Service
Based on the status of the ansible playbook run, a mock call is stubbed out representing the Incident management system. When invoking the incident creation call, a decision logic is checked to see if an incident is created in the last 24 hrs. The decision logic is defined as a business rule using Decision Model Notation.
This is a stub to plugin the REST API call to the incident management system.
```
cd create-incident
mvn fabric8:deloy
cd ..
```