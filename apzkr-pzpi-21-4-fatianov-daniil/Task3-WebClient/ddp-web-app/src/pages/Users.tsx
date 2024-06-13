import React, {useEffect, useState, useContext} from 'react';
import DataTable from '../components/DataTable';
import {fetchUsers} from '../services/UserService';
import {AuthContext} from '../context/AuthContext';

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

const tableHeader: string[] = [
    'Id',
    'Name',
    'Surname',
    'Email',
    'Phone',
    'Birthday',
    'Gender',
    'Date of creation',
];

const Users: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const authContext = useContext(AuthContext);

    useEffect(() => {
        console.log("Authenticated:", authContext?.isAuthenticated);
        console.log("Token:", authContext?.token);
        const loadData = async () => {
            if (authContext?.isAuthenticated && authContext.token) {
                try {
                    const usersData = await fetchUsers(authContext.token);
                    setUsers(usersData);
                } catch (error) {
                    console.error('Error fetching users:', error);
                }
            }
        };
        loadData();
    }, [authContext]);

    const adaptedUsers = users.map(user => ({
        id: user.id,
        name: user.name,
        surname: user.surname,
        email: user.email,
        phone: user.phone,
        birthday: new Date(user.birthday).toLocaleDateString(undefined, {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        }),
        gender: user.gender,
        'Date of creation': new Date(user.creationDate).toLocaleString()
    }));

    return (
        <>
            <h1>Users</h1>
            <DataTable tableHeader={tableHeader} tableRows={adaptedUsers}/>
        </>
    );
};

export default Users;