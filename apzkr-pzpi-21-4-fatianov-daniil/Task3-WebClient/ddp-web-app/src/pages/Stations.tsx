import React, {useEffect, useState, useContext} from 'react';
import {AuthContext} from '../context/AuthContext';
import {fetchStations} from '../services/StationService';
import DataTable from '../components/DataTable';
import {Button} from '@mui/material';
import {useNavigate} from 'react-router-dom';

interface Station {
    id: number;
    number: string;
    description: string;
    latitude: number;
    longitude: number;
    altitude: number;
    type: string;
    vehicles: { number: string; status: string }[];
}

interface AdaptedStation {
    id: number;
    number: string;
    description: string;
    latitude: string;
    longitude: string;
    altitude: string;
    type: string;
    vehicleNumbers: string;
}

const tableHeader: string[] = [
    'Id',
    'Number',
    'Description',
    'Latitude',
    'Longitude',
    'Altitude',
    'Type',
    'Vehicle Numbers',
    'Action'
];


const Stations: React.FC = () => {
    const [rawStations, setRawStations] = useState<Station[]>([]);
    const authContext = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        const loadData = async () => {
            if (authContext?.isAuthenticated && authContext.token) {
                try {
                    const fetchedStations = await fetchStations(authContext.token);
                    setRawStations(fetchedStations);
                } catch (error) {
                    console.error('Error fetching stations:', error);
                }
            }
        };
        loadData();
    }, [authContext?.isAuthenticated, authContext?.token]);

    const adaptedStations = rawStations.map(station => ({
        id: station.id,
        number: station.number,
        description: station.description,
        latitude: station.latitude,
        longitude: station.longitude,
        altitude: station.altitude,
        type: station.type,
        vehicleNumbers: station.vehicles.map(v => v.number).join(', ')
    }));


    return (
        <>
            <h1>Stations</h1>
            <Button variant="contained" onClick={() => navigate('/stations/create')}>
                Create
            </Button>
            <DataTable
                tableHeader={tableHeader}
                tableRows={adaptedStations}
                tableButton={{
                    buttonAction: (rowData) => {
                        navigate(`/stations/change/${rowData.id}`);
                    },
                    buttonLabel: 'Change'
                }}
            />
        </>
    );
};

export default Stations;