import React, {useState, useContext} from 'react';
import {useNavigate} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import {addStation} from '../services/StationService';
import {Box, Button, TextField} from '@mui/material';
import Dropdown from '../components/Dropdown';

const StationCreate: React.FC = () => {
    const navigate = useNavigate();
    const authContext = useContext(AuthContext);
    const token = authContext?.token;
    const [description, setDescription] = useState('');
    const [latitude, setLatitude] = useState('');
    const [longitude, setLongitude] = useState('');
    const [altitude, setAltitude] = useState('');
    const [type, setType] = useState('');

    const types = [
        {label: 'BASING', value: 'BASING'},
        {label: 'RECEIVING', value: 'RECEIVING'},
    ];

    const handleSave = async () => {
        if (token && description && latitude && longitude && altitude && type) {
            const stationData = {
                description,
                latitude: parseFloat(latitude),
                longitude: parseFloat(longitude),
                altitude: parseFloat(altitude),
                type
            };
            try {
                await addStation(stationData, token);
                navigate('/stations/');
            } catch (error) {
                console.error('Failed to add station:', error);
            }
        }
    };

    return (
        <div>
            <h1>Create Station</h1>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    width: 450,
                    margin: 'auto',
                    '& .MuiTextField-root': {marginBottom: 2}
                }}
            >
                <TextField
                    label="Description"
                    variant="outlined"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
                <TextField
                    label="Latitude"
                    type="number"
                    variant="outlined"
                    value={latitude}
                    onChange={(e) => setLatitude(e.target.value)}
                />
                <TextField
                    label="Longitude"
                    type="number"
                    variant="outlined"
                    value={longitude}
                    onChange={(e) => setLongitude(e.target.value)}
                />
                <TextField
                    label="Altitude"
                    type="number"
                    variant="outlined"
                    value={altitude}
                    onChange={(e) => setAltitude(e.target.value)}
                />
                <Dropdown
                    label="Type"
                    options={types}
                    value={type}
                    onChange={setType}
                />
                <Button
                    type="submit"
                    variant="contained"
                    onClick={handleSave}
                    disabled={!description || !latitude || !longitude || !altitude || !type}
                >
                    Save
                </Button>
            </Box>
        </div>
    );
};

export default StationCreate;