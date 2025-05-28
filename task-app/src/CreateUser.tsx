import {type FieldError, type SubmitHandler, useForm} from "react-hook-form"
import {Button, Form} from "react-bootstrap";
import * as React from "react";
import axios from "axios";

interface Inputs {
    username: string
    password: string
    firstName: string
    lastName: string
}

const ErrorForField: React.FC<{ error: FieldError | undefined }> = ({error}: { error: FieldError | undefined }) => {
    return (
        error && <Form.Control.Feedback type="invalid">{error.message}</Form.Control.Feedback>
    );
}

interface CreateUserResponse {
    id: string;
}

export function CreateUser() {
    const {
        register,
        handleSubmit,
        formState: {errors},
        setError,
        clearErrors,
    } = useForm<Inputs>()

    const onSubmit: SubmitHandler<Inputs> = (data) => {
        clearErrors();
        axios.post<CreateUserResponse>("/users", data)
            .then(response => {
                alert(`User created with id ${response.data.id}`);
            })
            .catch((error: unknown) => {
                if (axios.isAxiosError(error)) {
                    if (error.status == 409) {
                        setError("username", {
                            type: "manual",
                            message: "Username already registered",
                        }, {
                            shouldFocus: true,
                        })
                    } else {
                        console.log("Error occurred when registering user", error);
                        alert("Unexpected error occurred, please try again");
                    }
                }
            });
    }

    return (
        // eslint-disable-next-line @typescript-eslint/no-misused-promises
        <Form onSubmit={handleSubmit(onSubmit)}>
            <Form.Label htmlFor="username">Username</Form.Label>
            <Form.Control id="username" {...register("username", {required: 'Username is required'})}
                          isInvalid={!!errors.username}/>
            <ErrorForField error={errors.username}/>
            <br/>
            <Form.Label htmlFor="password">Password</Form.Label>
            <Form.Control id="password" type="password" {...register("password", {required: 'Password is required'})}
                          isInvalid={!!errors.password}/>
            <ErrorForField error={errors.password}/>
            <br/>
            <Form.Label htmlFor="firstName">First name</Form.Label>
            <Form.Control id="firstName" {...register("firstName", {required: 'First name is required'})}
                          isInvalid={!!errors.firstName}/>
            <ErrorForField error={errors.firstName}/>
            <br/>
            <Form.Label htmlFor="lastName">Last name</Form.Label>
            <Form.Control id="lastName" {...register("lastName", {required: 'Last name is required'})}
                          isInvalid={!!errors.lastName}/>
            <ErrorForField error={errors.lastName}/>
            <br/>
            <Button type="submit" variant="primary">Create user</Button>
        </Form>
    )
}