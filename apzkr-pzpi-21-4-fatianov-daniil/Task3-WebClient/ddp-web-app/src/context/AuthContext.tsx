import React, {createContext, useState, ReactNode, useContext, useEffect} from 'react';
import {AuthService} from '../services/AuthService';

interface AuthContextProps {
    isAuthenticated: boolean;
    token?: string | null;
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextProps | undefined>(undefined);

const AuthProvider: React.FC<{ children: ReactNode }> = ({children}) => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
    const [token, setToken] = useState<string | null>(localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!).token : null);

    useEffect(() => {
        const user = localStorage.getItem('user');
        if (user) {
            setIsAuthenticated(true);
            setToken(JSON.parse(user).token);
        }
    }, []);

    const login = async (email: string, password: string) => {
        const result = await AuthService.login(email, password);
        if (result.isAuthenticated && result.token) {
            localStorage.setItem('user', JSON.stringify({token: result.token}));
            setToken(result.token);
            setIsAuthenticated(true);
        } else {
            console.error('Login failed or user is not an administrator.');
            setIsAuthenticated(false);
        }
    };

    const logout = () => {
        localStorage.removeItem('user');
        setToken(null);
        setIsAuthenticated(false);
    };

    return (
        <AuthContext.Provider value={{isAuthenticated, token, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
};

export {AuthContext, AuthProvider};