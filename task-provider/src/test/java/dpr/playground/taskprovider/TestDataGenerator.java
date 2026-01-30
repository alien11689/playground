package dpr.playground.taskprovider;

import java.util.UUID;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.CreateProjectRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;

public class TestDataGenerator {
    
    public static class UserGenerator {
        public static String randomUsername() {
            return "user_" + UUID.randomUUID().toString().replace("-", "");
        }
        
        public static String randomPassword() {
            return "pass_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        
        public static String randomFirstName() {
            return "FirstName" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        
        public static String randomLastName() {
            return "LastName" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        
        public static CreateUserDTO randomUserDTO() {
            return new CreateUserDTO(randomUsername(), randomPassword(), randomFirstName(), randomLastName());
        }
    }
    
    public static class ProjectGenerator {
        public static String randomProjectName() {
            return "Project_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        
        public static String randomProjectDescription() {
            return "Description for project " + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        
        public static CreateProjectRequestDTO randomProjectRequestDTO() {
            var request = new CreateProjectRequestDTO();
            request.setName(randomProjectName());
            request.setDescription(randomProjectDescription());
            return request;
        }
    }
    
    public static class TaskGenerator {
        public static String randomTaskSummary() {
            return "Task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        
        public static String randomTaskDescription() {
            return "Description for task " + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        
        public static AddTaskRequestDTO randomTaskRequestDTO(UUID projectId) {
            var request = new AddTaskRequestDTO();
            request.setSummary(randomTaskSummary());
            request.setDescription(randomTaskDescription());
            request.setProjectId(projectId);
            return request;
        }
    }
    
    public static class CommentGenerator {
        public static String randomCommentContent() {
            return "Comment " + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        
        public static AddTaskCommentRequestDTO randomCommentRequestDTO() {
            var request = new AddTaskCommentRequestDTO();
            request.setContent(randomCommentContent());
            return request;
        }
    }
    
    public static class AuthGenerator {
        public static String randomBearerToken() {
            return "token_" + UUID.randomUUID().toString().replace("-", "") + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
