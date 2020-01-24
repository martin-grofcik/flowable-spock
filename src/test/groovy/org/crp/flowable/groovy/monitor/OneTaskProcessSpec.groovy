package org.crp.flowable.groovy.monitor

import org.crp.flowable.spock.PluggableFlowableSpecification

class OneTaskProcessSpec extends PluggableFlowableSpecification {

    def startOneTaskProcess() {
        given:
        def deployment = repositoryService.createDeployment().addClasspathResource("org/crp/flowable/groovy/monitor/oneTask.bpmn20.xml").deploy()
        when:
        runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("oneTaskProcess").
                start()
        then:
        assert runtimeService.createProcessInstanceQuery().count() == 1
        cleanup:
        repositoryService.deleteDeployment(deployment.id, true)
    }

}
