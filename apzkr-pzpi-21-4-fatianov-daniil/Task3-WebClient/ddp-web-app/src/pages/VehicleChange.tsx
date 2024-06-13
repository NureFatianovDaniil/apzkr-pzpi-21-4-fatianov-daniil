import React, {useEffect, useState, useContext} from 'react';
import {useParams, useNavigate} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import {changeVehicle, fetchVehicles} from '../services/VehicleService'; // переконайтеся, що import правильний
import {Box, Button, TextField} from '@mui/material';

interface Vehicle {
    id: number;
    number: string;
    flightDistance: number;
    liftingCapacity: number;
}

const VehicleChange: React.FC = () => {
    const {id} = useParams<{ id: string }>();
    const authContext = useContext(AuthContext);
    const navigate = useNavigate();
    const token = authContext?.token;
    const [vehicle, setVehicle] = useState<Vehicle | null>(null);
    const [vehicles, setVehicles] = useState<Vehicle[]>([]);

    useEffect(() => {
        fetchVehicles(token!).then(setVehicles);
    }, [token]);

    useEffect(() => {
        const foundVehicle = vehicles.find(v => v.id === parseInt(id!));
        setVehicle(foundVehicle ?? null);
    }, [id, vehicles]);

    const handleSave = async () => {
        if (token && vehicle) {
            try {
                await changeVehicle({
                    id: vehicle.id,
                    number: vehicle.number,
                    flightDistance: vehicle.flightDistance,
                    liftingCapacity: vehicle.liftingCapacity
                }, token);
                navigate('/vehicles/');
            } catch (error) {
                console.error('Failed to update vehicle:', error);
            }
        }
    };

    if (!vehicle) return <div>Loading or vehicle not found...</div>;

    return (
        <Box sx={{
            display: 'flex',
            flexDirection: 'column',
            width: 450,
            margin: 'auto'
        }}>
            <h1>Change Vehicle Details</h1>
            <TextField
                label="Number"
                variant="outlined"
                value={vehicle.number}
                onChange={(e) => setVehicle({...vehicle, number: e.target.value})}
                sx={{marginBottom: 2}}
            />
            <TextField
                label="Flight Distance"
                type="number"
                variant="outlined"
                value={vehicle.flightDistance.toString()}
                onChange={(e) => setVehicle({...vehicle, flightDistance: Number(e.target.value)})}
                sx={{marginBottom: 2}}
            />
            <TextField
                label="Lifting Capacity"
                type="number"
                variant="outlined"
                value={vehicle.liftingCapacity.toString()}
                onChange={(e) => setVehicle({...vehicle, liftingCapacity: Number(e.target.value)})}
                sx={{marginBottom: 2}}
            />
            <Button onClick={handleSave} variant="contained" color="primary">
                Save
            </Button>
            <Button variant="contained" color="secondary" sx={{marginTop: 2}}>
                Send
            </Button>
        </Box>
    );
};

export default VehicleChange;
