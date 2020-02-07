package org.crp.flowable.spock.examples

import org.crp.flowable.spock.Deployment
import org.crp.flowable.spock.PluggableFlowableSpecification
import org.flowable.bpmn.model.ReceiveTask
import org.flowable.engine.runtime.ProcessInstance

import static org.assertj.core.api.Assertions.assertThat
import static org.crp.flowable.spock.util.ProcessModelBuilder.*

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

    @Newify(ReceiveTask)
    def 'create script task process with builder'() {
        given:
            deploy model('scriptTaskProcess') >> startEvent() >> scriptTask(id: 'scriptTask', scriptFormat: 'groovy',
                    script: '''
                        execution.with {
                            setVariable 'newVariable', 'newVariableValue'
                        }
                    '''
            ) >> ReceiveTask(id:'receiveTask') >> endEvent()

        when:
            ProcessInstance pi = runtimeService.createProcessInstanceBuilder().
                processDefinitionKey("scriptTaskProcess").
                start()

        then:
            assert runtimeService.hasVariable(pi.getId(), 'newVariable')
            assertThat runtimeService.getVariable(pi.getId(), 'newVariable') isEqualTo 'newVariableValue'
    }

}
