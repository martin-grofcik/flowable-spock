package org.crp.flowable.groovy.monitor

import org.flowable.engine.impl.test.PluggableFlowableTestCase
import org.flowable.engine.test.Deployment
import org.junit.jupiter.api.Test

class OneTaskProcessTest extends PluggableFlowableTestCase {

	@Test
	@Deployment(resources = "org/crp/flowable/groovy/monitor/oneTask.bpmn20.xml")
	void "start one task process"() {
		runtimeService.createProcessInstanceBuilder().
				processDefinitionKey("oneTaskProcess").
				start()

		assert runtimeService.createProcessInstanceQuery().count() == 1
	}

	void "start one task process with method deployment"() {
		deployOneTaskTestProcess()

		runtimeService.createProcessInstanceBuilder().
				processDefinitionKey("oneTaskProcess").
				start()

		assert runtimeService.createProcessInstanceQuery().count() == 1
	}

}
