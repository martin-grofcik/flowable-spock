/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.crp.flowable.spock.internal

import org.flowable.bpmn.model.*
import org.flowable.engine.*
import org.flowable.engine.impl.ProcessEngineImpl
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.flowable.engine.impl.history.DefaultHistoryManager
import org.flowable.engine.impl.history.HistoryManager
import org.flowable.engine.repository.Deployment
import org.flowable.engine.repository.ProcessDefinition
import org.flowable.job.api.HistoryJob
import spock.lang.Specification

abstract class InternalFlowableSpecification extends Specification {
    protected static List<String> deploymentIdsForAutoCleanup = new ArrayList<>()

    protected static ProcessEngineConfigurationImpl processEngineConfiguration
    protected static ProcessEngine processEngine
    protected RepositoryService repositoryService
    protected RuntimeService runtimeService
    protected TaskService taskService
    protected FormService formService
    protected HistoryService historyService
    protected IdentityService identityService
    protected ManagementService managementService
    protected DynamicBpmnService dynamicBpmnService
    protected ProcessMigrationService processMigrationService

    @SuppressWarnings("unused")
    def setupSpec() {
        if (processEngine == null) {
            processEngine = createProcessEngine()
        }
        processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration()
    }

    protected abstract ProcessEngine createProcessEngine()

    protected static void doFinally() {
        ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()
        boolean isAsyncHistoryEnabled = processEngineConfiguration.isAsyncHistoryEnabled()

        if (isAsyncHistoryEnabled) {
            ManagementService managementService = processEngine.getManagementService()
            List<HistoryJob> jobs = managementService.createHistoryJobQuery().list()
            for (HistoryJob job : jobs) {
                managementService.deleteHistoryJob(job.getId())
            }
        }

        HistoryManager asyncHistoryManager = null
        try {
            if (isAsyncHistoryEnabled) {
                processEngineConfiguration.setAsyncHistoryEnabled(false)
                asyncHistoryManager = processEngineConfiguration.getHistoryManager()
                processEngineConfiguration
                        .setHistoryManager(new DefaultHistoryManager(processEngineConfiguration,
                                processEngineConfiguration.getHistoryLevel(), processEngineConfiguration.isUsePrefixId()))
            }

            cleanDeployments(processEngine)

        } finally {

            if (isAsyncHistoryEnabled) {
                processEngineConfiguration.setAsyncHistoryEnabled(true)
                processEngineConfiguration.setHistoryManager(asyncHistoryManager)
            }

            processEngineConfiguration.getClock().reset()
        }
    }

    protected static void cleanDeployments(ProcessEngine processEngine) {
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration()
        for (String autoDeletedDeploymentId : deploymentIdsForAutoCleanup) {
            processEngineConfiguration.getRepositoryService().deleteDeployment(autoDeletedDeploymentId, true)
        }
        deploymentIdsForAutoCleanup.clear()
    }

    String deploy(String classpathResource) {
        return repositoryService.createDeployment().addClasspathResource(classpathResource).deploy().getId()
    }

    /**
     * Creates and deploys the one task process. See {@link #createOneTaskTestProcess()}.
     *
     * @return The process definition id (NOT the process definition key) of deployed one task process.
     */
    String deployOneTaskTestProcess() {
        BpmnModel bpmnModel = createOneTaskTestProcess()
        Deployment deployment = repositoryService.createDeployment().addBpmnModel("oneTasktest.bpmn20.xml", bpmnModel).deploy()

        deploymentIdsForAutoCleanup.add(deployment.getId()) // For auto-cleanup

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult()
        return processDefinition.getId()
    }

    static BpmnModel createOneTaskTestProcess() {
        BpmnModel model = new BpmnModel()
        org.flowable.bpmn.model.Process process = createOneTaskProcess()
        model.addProcess(process)

        return model
    }

    static org.flowable.bpmn.model.Process createOneTaskProcess() {
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process()
        process.setId("oneTaskProcess")
        process.setName("The one task process")

        StartEvent startEvent = new StartEvent()
        startEvent.setId("start")
        startEvent.setName("The start")
        process.addFlowElement(startEvent)

        UserTask userTask = new UserTask()
        userTask.setName("The Task")
        userTask.setId("theTask")
        userTask.setAssignee("kermit")
        process.addFlowElement(userTask)

        EndEvent endEvent = new EndEvent()
        endEvent.setId("theEnd")
        endEvent.setName("The end")
        process.addFlowElement(endEvent)

        process.addFlowElement(new SequenceFlow("start", "theTask"))
        process.addFlowElement(new SequenceFlow("theTask", "theEnd"))

        return process
    }

}
