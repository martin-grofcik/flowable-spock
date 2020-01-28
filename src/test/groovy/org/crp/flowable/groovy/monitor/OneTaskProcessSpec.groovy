package org.crp.flowable.groovy.monitor

import org.crp.flowable.spock.Deployment
import org.crp.flowable.spock.PluggableFlowableSpecification

class OneTaskProcessSpec extends PluggableFlowableSpecification {

    def "start one task process with repositoryService deployment"() {
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

    def "start one task process with method deployment"() {
        given:
            deployOneTaskTestProcess()
        when:
            runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("oneTaskProcess").
                start()
        then:
            assert runtimeService.createProcessInstanceQuery().count() == 1
    }

    @Deployment(resources = ["org/crp/flowable/groovy/monitor/oneTask.bpmn20.xml"])
    def "start one task process with annotation deployment"() {
        when:
            runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("oneTaskProcess").
                start()
        then:
            assert runtimeService.createProcessInstanceQuery().count() == 1
    }

}
