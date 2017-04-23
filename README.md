#Health-Id Service
This is a service which provides below operations related to Health-Ids
for MCI and other Organizations.
* Generation and allocation of HelthIds for an Organization.
* Tracking of a particular Health-Id allocated to an organization.
## Setting up the environment

###Prerequisites

* [VirtualBox](https://www.virtualbox.org/)
* [Vagrant](http://docs.vagrantup.com/v2/installation/index.html)
* [Ansible](https://www.ansible.com/)

####Steps to setup environment on a VM and get Health-Id service working with Stub Identity Server.
##### Checkout the following repositories (under a common parent directory)
* [FreeSHR-Playbooks](https://github.com/SharedHealth/FreeSHR-Playbooks)
* [HealthId-Service](https://github.com/sharedHealth/healthid-service)
* [Identity-Server](https://github.com/SharedHealth/Identity-Server)

##### Setup ansible group_vars(Need to verify)
* replace FreeSHR-Playbooks/group_vars/all with FreeSHR-Playbooks/group_vars/all_example
* create a dummy ansible vault pass file in your user home folder.
```
touch ~/.vaultpass.txt
```

##### Build Identity-Server
* ./gradlew clean dist
* cp build/distributions/identity-server-*.noarch.rpm /tmp/


##### Build HealthId-Service
* ./gradlew clean dist
* cp healthId-api/build/distributions/healthId-*.noarch.rpm /tmp/
* vagrant up | vagrant provision

Notes:
- The above will provision and deploy, HealthId-Service and a Stub Identity Server in 192.168.33.19. Cassandra is installed as a single node cluster.
- If you find cassandra not running, ssh to the vagrant box and start cassandra (service cassandra start)
- HealthId-Service will run on port 8086
- Stub Identity Server will run in port 8084


### Generate some IDs
To generate ids you need to follow below steps 
 

Example steps:
* Login to IdP as an SHR System Admin
  * `curl http://192.168.33.19:8084/signin -H "X-Auth-Token:local-user-auth-token" -H "client_id:18700" --form "email=local-user@test.com" --form "password=password"`
  * This should return you an access_token for SHR System Admin.

* With the above token for SHR System Admin, now you can POST to the http://192.168.33.19:8086/healthIds/generateBlock?start=9800000100&totalHIDs=100 to generate Health IDs with below headers
  * X-Auth-Token:{the token you received in the previous step}
  * client_id:18700 { this is client id for the user who signed in}
  * From: local-user@test.com

* The above should return you a message saying "Generated 100 HIDs".
* You can login to cassandra and check the same
  *  cqlsh 192.168.33.19 -ucassandra -ppassword
  * use healthid ;
  * select count(*) from mci_healthid ;
  
* Above should return count as 100.     


The stub IdP doesn't expire the token unless the Identity-Service is restarted. So you can keep using the "access_token". In reality, the access_token is short-lived and also can be invalidated.


### Troubleshooting
* you might have to install sshpass. Please refer here [here](http://www.nextstep4it.com/sshpass-command-non-interactive-ssh/)
