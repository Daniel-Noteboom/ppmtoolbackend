package com.practice_project.ppmtool.services;

import com.practice_project.ppmtool.domain.Backlog;
import com.practice_project.ppmtool.domain.Project;
import com.practice_project.ppmtool.domain.User;
import com.practice_project.ppmtool.exceptions.ProjectIdException;
import com.practice_project.ppmtool.exceptions.ProjectNotFoundException;
import com.practice_project.ppmtool.repositories.BacklogRepository;
import com.practice_project.ppmtool.repositories.ProjectRepository;
import com.practice_project.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private UserRepository userRepository;

    public Project saveOrUpdateProject(Project project, String username) {

        if(project.getId() != null){
            Project existingProject = projectRepository.findByProjectIdentifier(project.getProjectIdentifier());
            if(existingProject !=null &&(!existingProject.getProjectLeader().equals(username))){
                throw new ProjectNotFoundException("Project not found in your account");
            }else if(existingProject == null){
                throw new ProjectNotFoundException("Project with ID: '"+project.getProjectIdentifier()+"' cannot be updated because it doesn't exist");
            }
        }

        try {
            User user = userRepository.findByUsername(username);
            project.setUser(user);
            project.setProjectLeader(user.getUsername());
            String uppercaseIdentifier = project.getProjectIdentifier().toUpperCase();
            project.setProjectIdentifier(uppercaseIdentifier);
            if(project.getId() == null) {
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(uppercaseIdentifier);
            }

            if(project.getId() != null) {
                project.setBacklog(backlogRepository.findByProjectIdentifier(uppercaseIdentifier));
            }
            return projectRepository.save(project);
        } catch(Exception e) {
            throw new ProjectIdException("Project ID '" + project.getProjectIdentifier().toUpperCase() + "' already exists");
        }
    }

    public Project findProjectByIdentifier(String projectId, String username){

        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());
        if(project == null) {
            throw new ProjectIdException("Project ID '" + projectId.toUpperCase() + "' does not exist");
        }

        if(!project.getProjectLeader().equals(username)){
            throw new ProjectNotFoundException("Project not found in your account");
        }

        return project;
    }

    public Iterable<Project> findAllProjects(String username){
        return projectRepository.findAllByProjectLeader(username);
    }
    public void deleteProjectByIdentifier(String projectid, String username){
        projectRepository.delete(findProjectByIdentifier(projectid, username));
    }

}
