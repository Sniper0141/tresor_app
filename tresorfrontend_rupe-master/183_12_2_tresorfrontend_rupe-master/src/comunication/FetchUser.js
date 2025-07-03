/**
 * Fetch methodes for user api calls
 * @author Peter Rutschmann
 */


export const getUsers = async () => {
    const protocol = process.env.REACT_APP_API_PROTOCOL; // "http"
    const host = process.env.REACT_APP_API_HOST; // "localhost"
    const port = process.env.REACT_APP_API_PORT; // "8080"
    const path = process.env.REACT_APP_API_PATH; // "/api"
    const portPart = port ? `:${port}` : ''; // port is optional
    const API_URL = `${protocol}://${host}${portPart}${path}`;

    try {
        const response = await fetch(`${API_URL}/users`, {
            method: 'Get',
            headers: {
                'Accept': 'application/json'
            },
            credentials: "include"
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Server response failed.');
        }

        const data = await response.json();
        console.log('User successfully got:', data);
        return data;
    } catch (error) {
        console.error('Failed to get user:', error.message);
        throw new Error('Failed to get user. ' || error.message);
    }
}

export const postUser = async (credentials, captchaToken) => {
    const protocol = process.env.REACT_APP_API_PROTOCOL; // "http"
    const host = process.env.REACT_APP_API_HOST; // "localhost"
    const port = process.env.REACT_APP_API_PORT; // "8080"
    const path = process.env.REACT_APP_API_PATH; // "/api"
    const portPart = port ? `:${port}` : ''; // port is optional
    const API_URL = `${protocol}://${host}${portPart}${path}`;

    try {
        const response = await fetch(`${API_URL}/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                firstName: `${credentials.firstName}`,
                lastName: `${credentials.lastName}`,
                email: `${credentials.email}`,
                password: `${credentials.password}`,
                passwordConfirmation: `${credentials.passwordConfirmation}`,
                recaptchaToken: `${captchaToken}`
            }),
            credentials: "include"
        });

        const body = await response.json();
        
        console.log(body);

        if(body !== ""){
            console.log('Registration failed: ', body);
            return body;
        }

        console.log('User successfully posted:', body);
        return body;
    } catch (error) {
        console.error('Failed to post user:', error.message);
        throw new Error('Failed to save user. ' || error.message);
    }
};

export const postLogin = async (content) => {
    const protocol = process.env.REACT_APP_API_PROTOCOL; // "http"
    const host = process.env.REACT_APP_API_HOST; // "localhost"
    const port = process.env.REACT_APP_API_PORT; // "8080"
    const path = process.env.REACT_APP_API_PATH; // "/api"
    const portPart = port ? `:${port}` : ''; // port is optional
    const API_URL = `${protocol}://${host}${portPart}${path}`;

    try {
        const response = await fetch(`${API_URL}/users/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: `${content.email}`,
                password: `${content.password}`
            }),
            credentials: "include"
        });
        console.log("RESPONSE CAME \n" + response)

        const data = await response.json();

        if(response.status === 200){
            console.log('Login successfully posted:', data);
            document.cookie = `jwt=${data.jwt}; Path=/; Max-Age=86400`
            return null;
        }

        console.warn("Login didn't pass:", data);
        return "Login failed: " + data.message;
    } catch (error) {
        console.error('Failed to post login:', error.message);
        throw new Error('Failed to do login. ' || error.message);
    }
};