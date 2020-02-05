package org.crp.flowable.spock.examples

import org.crp.flowable.spock.Deployment
import org.crp.flowable.spock.PluggableFlowableSpecification

import static org.crp.flowable.spock.util.ProcessModelBuilder.endEvent
import static org.crp.flowable.spock.util.ProcessModelBuilder.model
import static org.crp.flowable.spock.util.ProcessModelBuilder.startEvent
import static org.crp.flowable.spock.util.ProcessModelBuilder.userTask

class OneTaskProcessSpec extends PluggableFlowableSpecification {

    def "start one task process with repositoryService deployment"() {
        given:
            def deployment = repositoryService.createDeployment().addClasspathResource("org/crp/flowable/spock/examples/oneTask.bpmn20.xml").deploy()
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

    @Deployment(resources = ["org/crp/flowable/spock/examples/oneTask.bpmn20.xml"])
    def "start one task process with annotation deployment"() {
        when:
            runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("oneTaskProcess").
                start()
        then:
            assert runtimeService.createProcessInstanceQuery().count() == 1
    }

    def "start one task process with given deployment"() {
        given:
            deploy "org/crp/flowable/spock/examples/oneTask.bpmn20.xml"
        when:
            runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("oneTaskProcess").
                start()
        then:
            assert runtimeService.createProcessInstanceQuery().count() == 1
    }

    def 'create one task process with builder'() {
        given:
            deploy model('oneTaskProcess') >> startEvent() >> userTask(id: 'userTask') >> endEvent()
        when:
            runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("oneTaskProcess").
                start()
        then:
            assert runtimeService.createProcessInstanceQuery().count() == 1
    }
}
