import {type SubmitHandler, useForm} from "react-hook-form"
import {Button, Form} from "react-bootstrap";
import * as React from "react";

type Inputs = {
    username: string
    password: string
    firstName: string
    lastName: string
}

const InvalidValueForField: React.FC<{ children: string }> = ({children}: { children: string }) => {
    return (
        <Form.Control.Feedback type="invalid">{children}</Form.Control.Feedback>
    );
}

export function CreateUser() {
    const {
        register,
        handleSubmit,
        formState: {errors},
    } = useForm<Inputs>()
    const onSubmit: SubmitHandler<Inputs> = (data) => alert(`Creating user ${JSON.stringify(data)}`);

    return (
        <Form onSubmit={handleSubmit(onSubmit)}>
            <Form.Label htmlFor="username">Username</Form.Label>
            <Form.Control id="username" {...register("username", {required: true})} isInvalid={!!errors.username}/>
            {errors.username && <InvalidValueForField>Username is required</InvalidValueForField>}
            <br/>
            <Form.Label htmlFor="password">Password</Form.Label>
            <Form.Control id="password" type="password" {...register("password", {required: true})}
                          isInvalid={!!errors.password}/>
            {errors.password && <InvalidValueForField>Password is required</InvalidValueForField>}
            <br/>
            <Form.Label htmlFor="firstName">First name</Form.Label>
            <Form.Control id="firstName" {...register("firstName", {required: true})} isInvalid={!!errors.firstName}/>
            {errors.firstName && <InvalidValueForField>First name is required</InvalidValueForField>}
            <br/>
            <Form.Label htmlFor="lastName">Last name</Form.Label>
            <Form.Control id="lastName" {...register("lastName", {required: true})} isInvalid={!!errors.lastName}/>
            {errors.lastName && <InvalidValueForField>Last name is required</InvalidValueForField>}
            <br/>
            <Button type="submit" variant="primary">Create user</Button>
        </Form>
    )
}