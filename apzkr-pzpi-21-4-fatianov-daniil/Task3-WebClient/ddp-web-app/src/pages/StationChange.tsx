import React, {useEffect, useState, useContext} from 'react';
import {useParams, useNavigate} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import {changeStation, fetchStations} from '../services/StationService';
import {Box, Button, TextField, Typography} from '@mui/material';

interface Station {
    id: number;
    number: string;
    description: string;
    latitude: number;
    longitude: number;
    altitude: number;
    type: string;
}

const StationChange: React.FC = () => {
    const {id} = useParams<{ id: string }>();
    const authContext = useContext(AuthContext);
    const navigate = useNavigate();
    const token = authContext?.token;
    const [station, setStation] = useState<Station | null>(null);
    const [stations, setStations] = useState<Station[]>([]);

    useEffect(() => {
        if (token) {
            fetchStations(token).then(data => {
                setStations(data);
                const foundStation = data.find(st => st.id === parseInt(id!));
                setStation(foundStation ?? null);
            }).catch(console.error);
        }
    }, [token, id]);

    const handleSave = async () => {
        if (token && station) {
            try {
                await changeStation({
                    number: station.number,
                    description: station.description,
                    latitude: station.latitude,
                    longitude: station.longitude,
                    altitude: station.altitude,
                    type: station.type
                }, token);
                navigate('/stations/');
            } catch (error) {
                console.error('Failed to update station:', error);
            }
        }
    };

    if (!station) return <div>Loading or station not found...</div>;

    return (
        <Box sx={{display: 'flex', flexDirection: 'column', width: 450, margin: 'auto'}}>
            <h1>Change Station Details</h1>
            <Typography variant="h6" sx={{marginBottom: 2}}>
                Number: {station.number}
            </Typography>
            <TextField
                label="Description"
                variant="outlined"
                value={station.description}
                onChange={(e) => setStation({...station, description: e.target.value})}
                sx={{marginBottom: 2}}
            />
            <TextField
                label="Latitude"
                type="number"
                variant="outlined"
                value={station.latitude.toString()}
                onChange={(e) => setStation({...station, latitude: parseFloat(e.target.value)})}
                sx={{marginBottom: 2}}
            />
            <TextField
                label="Longitude"
                type="number"
                variant="outlined"
                value={station.longitude.toString()}
                onChange={(e) => setStation({...station, longitude: parseFloat(e.target.value)})}
                sx={{marginBottom: 2}}
            />
            <TextField
                label="Altitude"
                type="number"
                variant="outlined"
                value={station.altitude.toString()}
                onChange={(e) => setStation({...station, altitude: parseFloat(e.target.value)})}
                sx={{marginBottom: 2}}
            />
            <Button onClick={handleSave} variant="contained" color="primary">
                Save
            </Button>
            <Button variant="contained" color="secondary" sx={{marginTop: 2}}>
                Delete
            </Button>
        </Box>
    );
};

export default StationChange;
