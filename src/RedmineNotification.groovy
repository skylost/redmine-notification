import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode

// See http://rundeck.org/docs/developer/notification-plugin-development.html
// See https://github.com/rundeck-plugins/pagerduty-notification


// Defaults parameters
class DEFAULTS {
  static String REDMINE_URL="https://redmine.init/issues.json"
  static String REDMINE_PROJECT="277"
  static String REDMINE_PRIORITY_ID="4"
  static String REDMINE_TRACKER_ID="7"
  static String SUBJECT_LINE='RUNDECK => [${job.project}] [${job.name}] => ${job.status}'
  static String DESCRIPTION_LINE='[${job.project}] job => \"${job.name}\" run by ${job.user} => ${job.status}'
}

/**
* Expands the Subject string using a predefined set of tokens
*/
def subjectString(text,binding) {
  //defines the set of tokens usable in the subject configuration property
  def tokens=[
    '${job.project}': binding.execution.job.project,
    '${job.name}': binding.execution.job.name,
    '${job.status}': binding.execution.status.toUpperCase(),    
  ]
  text.replaceAll(/(\$\{\S+?\})/){
    if(tokens[it[1]]){
      tokens[it[1]]
    } else {
      it[0]
    }
  }
}

def descriptionString(text,binding) {
  //defines the set of tokens usable in the subject configuration property
  def tokens=[
    '${job.project}': binding.execution.job.project,
    '${job.name}': binding.execution.job.name,
    '${job.user}': binding.execution.user,
    '${job.status}': binding.execution.status.toUpperCase(),    
  ]
  text.replaceAll(/(\$\{\S+?\})/){
    if(tokens[it[1]]){
      tokens[it[1]]
    } else {
      it[0]
    }
  }
}

/**
* Trigger of issues redmine
* @param executionData
* @param configuration
*/
def triggerEvent(Map executionData, Map configuration) {
  System.err.println("DEBUG: service_key="+configuration.service_key)
  System.err.println("DEBUG: redmine_Project="+configuration.project)
  def expandedSubject = subjectString(configuration.subject, [execution:executionData])
  def expandedDescription = descriptionString(configuration.description, [execution:executionData])
  // Event json
  def job_json = [
    issue:[
      project_id: configuration.project,
      subject: expandedSubject,
      priority_id: DEFAULTS.REDMINE_PRIORITY_ID,
      tracker_id: DEFAULTS.REDMINE_TRACKER_ID,
      description: expandedDescription,
    ]
  ]
    
  // Send the request.
  def url = new URL(DEFAULTS.REDMINE_URL)
  def connection = url.openConnection()
  connection.setRequestMethod("POST")
  connection.setRequestProperty("X-Redmine-API-Key", configuration.service_key)
  connection.setRequestProperty("Content-type", "application/json")
  connection.doOutput = true
  def writer = new OutputStreamWriter(connection.outputStream)
  def json = new ObjectMapper()
  writer.write(json.writeValueAsString(job_json))
  writer.flush()
  writer.close()
  connection.connect()
    
  // process the response.
  int responseCode = connection.getResponseCode();
  System.out.println("DEBUG: Code HTTP" + String.valueOf(responseCode));
  // Validate the response code
  if (responseCode == 401 || responseCode == 403) {
    System.err.println("ERROR: Unauthorized - Authentication is required to access the resource");
  } else if (responseCode == 200 || responseCode == 201) {
    System.out.println("Info: OK/Created - Request treated successfully with creating a document");  
    System.out.println("Info: redmine_json: " + connection.content.text)
  }

}

rundeckPlugin(NotificationPlugin){
  title="Redmine"
  description="Create a issues redmine."
  configuration{
    subject title:"Subject", description:"Incident subject line. Can contain \${job.status}, \${job.project}, \${job.name}", defaultValue:DEFAULTS.SUBJECT_LINE,required:true
    description title:"Description", description:"Incident description line. Can contain \${job.project}, \${job.name}, \${job.user}, \${job.status}", defaultValue:DEFAULTS.DESCRIPTION_LINE,required:true
    project title:"Project", description:"Define project name. Example : 277", defaultValue:DEFAULTS.REDMINE_PROJECT,required:true
    service_key title:"Service API Key", description:"The service key", scope:"Project"
  }
  
  onstart { Map executionData,Map configuration ->
    triggerEvent(executionData, configuration)
    true
  }
  onfailure { Map executionData, Map configuration ->
    triggerEvent(executionData, configuration)
    true
  }
  onsuccess { Map executionData, Map configuration ->
    triggerEvent(executionData, configuration)
    true
  }
}
