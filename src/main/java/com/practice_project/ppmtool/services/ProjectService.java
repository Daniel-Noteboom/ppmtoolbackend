package com.practice_project.ppmtool.services;

import com.practice_project.ppmtool.domain.Backlog;
import com.practice_project.ppmtool.domain.Project;
import com.practice_project.ppmtool.exceptions.ProjectIdException;
import com.practice_project.ppmtool.repositories.BacklogRepository;
import com.practice_project.ppmtool.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogRepository backlogRepository;

    public Project saveOrUpdateProject(Project project) {
        try {
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

    public Project findProjectByIdentifier(String projectId) {

        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());
        if(project == null) {
            throw new ProjectIdException("Project ID '" + projectId.toUpperCase() + "' does not exist");
        }

        return project;
    }

    public Iterable<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    public void deleteProjectByIdentifier(String projectId) {
        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());

        if(project == null) {
            throw new ProjectIdException("Cannot delete project with ID '" + projectId.toUpperCase() +
                    "'. This project does not exist");
        }

        projectRepository.delete(project);
    }

}
