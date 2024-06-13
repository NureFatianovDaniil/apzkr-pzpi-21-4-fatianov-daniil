import React, {useContext, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import {Button, TextField} from '@mui/material';

const Login: React.FC = () => {
    const authContext = useContext(AuthContext);
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    if (!authContext) return null;

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await authContext.login(username, password);
            navigate('/');
        } catch (error) {
            setError('Invalid username or password');
        }
    };

    if (authContext.isAuthenticated) {
        return (
            <>
                <h1>Login</h1>
                <Button onClick={() => authContext.logout()} variant="contained">
                    Logout
                </Button>
            </>
        );
    }

    return (
        <div>
            <h1>Login</h1>
            {error && <p style={{color: 'red'}}>{error}</p>}
            <form onSubmit={handleLogin}>
                <div>
                    <TextField
                        label="Username"
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <TextField
                        label="Password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <Button type="submit" variant="contained">
                    Login
                </Button>
            </form>
        </div>
    );
};

export default Login;