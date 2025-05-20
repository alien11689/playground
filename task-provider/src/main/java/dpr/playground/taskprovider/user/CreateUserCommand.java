package dpr.playground.taskprovider.user;

public record CreateUserCommand(String userName, String password, String firstName, String lastName) {
}
