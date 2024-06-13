import axios from 'axios';

interface User {
    id: number;
    name: string;
    surname: string;
    email: string;
    phone: string;
    birthday: string;
    gender: string;
    creationDate: string;
}

const fetchUsers = async (token: string): Promise<User[]> => {
    const response = await axios.get('http://localhost:8082/user-service/user/all', {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
    return response.data;
}

export {fetchUsers};