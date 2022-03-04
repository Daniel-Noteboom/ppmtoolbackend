package com.practice_project.ppmtool.services;

import com.practice_project.ppmtool.domain.Backlog;
import com.practice_project.ppmtool.domain.Project;
import com.practice_project.ppmtool.domain.ProjectTask;
import com.practice_project.ppmtool.exceptions.ProjectNotFoundException;
import com.practice_project.ppmtool.repositories.BacklogRepository;
import com.practice_project.ppmtool.repositories.ProjectRepository;
import com.practice_project.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTaskService {

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username){

    //Exceptions: Project not found
        //PTs to be added to a specific project, project != null, BL exists
        Backlog backlog =  projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog(); //backlogRepository.findByProjectIdentifier(projectIdentifier);
        //set the bl to pt
        projectTask.setBacklog(backlog);
        //We want our project sequence to be like this: IDPRO-1, IDPRO-2, ...100 101
        Integer backlogSequence = backlog.getPTSequence();
        //Update the bl Sequence
        backlogSequence++;
        backlog.setPTSequence(backlogSequence);
        //Add Sequence to Project Task
        projectTask.setProjectSequence(projectIdentifier + "-" + backlogSequence);
        projectTask.setProjectIdentifier(projectIdentifier);

        //INITIAL priority when priority null
        if (projectTask.getPriority() == null || projectTask.getPriority() == 0) {
            projectTask.setPriority(ProjectTask.HIGH_PRIORITY);
        }

        //INITIAL status when status is null
        if (projectTask.getStatus() == "" || projectTask.getStatus() == null) {
            projectTask.setStatus(ProjectTask.INITIAL_TASK);
        }

        return projectTaskRepository.save(projectTask);

    }


    public Iterable<ProjectTask>findBacklogById(String id, String username){

        //Make sure project exists and belongs to user
        projectService.findProjectByIdentifier(id, username);

        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence(String backlogId, String ptId, String username){

        //make sure we are searching on the right backlog
        projectService.findProjectByIdentifier(backlogId, username);


        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(ptId);
        if(projectTask == null) {
            throw new ProjectNotFoundException("Project Task '" + ptId + "' not found");
        }

        if(!projectTask.getBacklog().getProjectIdentifier().equals(backlogId)) {
            throw new ProjectNotFoundException("Project Task '" + ptId + "' does not exist in project: '" + backlogId);
        }
        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlogId, String ptId, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlogId, ptId, username);
        return projectTaskRepository.save(updatedTask);
    }

    public void deletePTByProjectSequence(String backlogId, String ptId, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlogId, ptId, username);
        projectTaskRepository.delete(projectTask);
    }
}
