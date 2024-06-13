import axios from 'axios';

interface LoginResponse {
    token: string;
}

interface AuthResult {
    isAuthenticated: boolean;
    token?: string;
}

const API_URL = 'http://localhost:8082/user-service/auth/';//8082

const login = async (email: string, password: string): Promise<AuthResult> => {
    try {
        const authResponse = await axios.post<LoginResponse>(API_URL + 'authenticate', {
            email,
            password
        });

        const {token} = authResponse.data;

        const isAdminResponse = await axios.get<boolean>(`${API_URL}is-admin?token=${token}`);

        if (isAdminResponse.data) {
            localStorage.setItem('user', JSON.stringify({token}));
            console.log('Token: ' + token);
            return {isAuthenticated: true, token};
        } else {
            throw new Error("User is not an administrator");
        }
    } catch (error) {
        console.error("Authentication failed:", error);
        return {isAuthenticated: false};
    }
};

export const AuthService = {
    login,
};