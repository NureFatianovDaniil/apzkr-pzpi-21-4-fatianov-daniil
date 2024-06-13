import React, {useEffect, useState, useContext} from 'react';
import {useNavigate} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import {addVehicle} from '../services/VehicleService';
import {fetchStationsBasingForDropDown} from '../services/StationService';
import {Box, Button, TextField} from '@mui/material';
import Dropdown from '../components/Dropdown';

interface DropdownOption {
    label: string;
    value: string;
}

const VehicleCreate: React.FC = () => {
    const navigate = useNavigate();
    const authContext = useContext(AuthContext);
    const token = authContext?.token;
    const [stations, setStations] = useState<DropdownOption[]>([]);
    const [station, setStation] = useState('');
    const [number, setNumber] = useState('');
    const [flightDistance, setFlightDistance] = useState('');
    const [liftingCapacity, setLiftingCapacity] = useState('');

    useEffect(() => {
        if (token) {
            fetchStationsBasingForDropDown(token).then(setStations).catch(console.error);
        }
    }, [token]);

    const handleSave = async () => {
        if (token && flightDistance && liftingCapacity && station) {
            const vehicleData = {
                flightDistance: parseInt(flightDistance),
                liftingCapacity: parseInt(liftingCapacity),
                stationNumber: station
            };
            try {
                await addVehicle(vehicleData, token);
                navigate('/vehicles/');
            } catch (error) {
                console.error('Failed to add vehicle:', error);
            }
        }
    };

    return (
        <div>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    width: 450,
                    margin: 'auto'
                }}
            >
                <h1>Create Vehicle</h1>
                <TextField
                    id="flight_distance"
                    label="Flight Distance"
                    variant="outlined"
                    value={flightDistance}
                    onChange={(e) => setFlightDistance(e.target.value)}
                    sx={{marginTop: 2}}
                />
                <TextField
                    id="lifting_capacity"
                    label="Lifting Capacity"
                    variant="outlined"
                    value={liftingCapacity}
                    onChange={(e) => setLiftingCapacity(e.target.value)}
                    sx={{marginTop: 2}}
                />
                <Dropdown
                    label="Station Number"
                    options={stations}
                    value={station}
                    onChange={setStation}
                />
                <Button
                    type="submit"
                    variant="contained"
                    onClick={handleSave}
                    disabled={!flightDistance || !liftingCapacity || !station}
                    sx={{marginTop: 2}}
                >
                    Save
                </Button>
            </Box>
        </div>
    );
};

export default VehicleCreate;