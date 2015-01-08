A Rundeck plugin implemented in Groovy

Use this [notification](http://rundeck.org/docs/developer/notification-plugin.html#groovy-plugin-type)
plugin to send [issues](http://www.redmine.org/projects/redmine/wiki/Rest_Issues)
events to your [Redmine](http://www.redmine.org) service.

=======

## Installation

Copy the groovy script to the plugins directory:

    cp src/RedmineNotification.groovy to $RDECK_BASE/libext

and start using it!


## Required parameters

The plugin requires five parameters :

* Subject: This string will be set as the subject for the generated incident.

Context variables usable in the subject line:

 * `${job.status}`: Job execution status (eg, FAILED, SUCCESS).
 * `${job.project}`: Job project name.
 * `${job.name}`: Job name.


* Description: This string will be set as the description for the generated incident.*

Context variables usable in the description line:

 * `${job.status}`: Job execution status (eg, FAILED, SUCCESS).
 * `${job.project}`: Job project name.
 * `${job.name}`: Job name.
 * `${job.user}`: User that executed the job.


* Project: This integer defines ID of the Redmine project.


* Tracker: This integer defines ID of the Redmine tracker.


* Priority: This integer defines ID of the Redmine priority.


## Configuration

The plugin requires three or five configurations entries.

* proxy: This configuration enable proxy settings:

Enable proxy setting in your instance level: $RDECK_BASE/etc/framework.properties

    framework.plugin.Notification.RedmineNotification.proxy = true

* proxy_url: This is the URL proxy setting 

Configure the URL proxy setting in your instance level: $RDECK_BASE/etc/framework.properties 

    framework.plugin.Notification.RedmineNotification.proxy_url = proxy.int

* proxy_port:

Configure the proxy port setting in your instance level: $RDECK_BASE/etc/framework.properties

    framework.plugin.Notification.RedmineNotification.proxy_port = 3128

* redmine_url: This is the redmine URL 

Configure the redmine_url in your instance level: $RDECK_BASE/etc/framework.properties

    framework.plugin.Notification.RedmineNotification.redmine_url = https://your.redmine.org/issues.json

* redmine_apikey: This is the API Key to your redmine service.

Configure the redmine_apikey in your instance level: $RDECK_BASE/etc/framework.properties

    framework.plugin.Notification.RedmineNotification.redmine_apikey = xx123049e89dd45f28ce35467a08577yz
