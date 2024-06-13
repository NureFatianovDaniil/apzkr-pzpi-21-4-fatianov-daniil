import React, {useEffect, useState, useContext} from 'react';
import {AuthContext} from '../context/AuthContext';
import {fetchVehicles} from '../services/VehicleService';
import DataTable from '../components/DataTable';
import {Button} from '@mui/material';
import {useNavigate} from 'react-router-dom';

interface Vehicle {
    id: number;
    number: string;
    liftingCapacity: number;
    flightDistance: number;
    stationNumber: string;
    order: {
        arrivalStationNumber: string,
        number: string,
        items: any[] | null,
    } | null;
    status: string;
}

interface AdaptedVehicle {
    id: number;
    number: string;
    liftingCapacity: string;
    flightDistance: string;
    stationNumber: string;
    orderNumber: string;
    status: string;
}

const tableHeader: string[] = [
    'Id',
    'Number',
    'Lifting Capacity',
    'Flight Distance',
    'Station Number',
    'Order Number',
    'Status',
    'Action'
];

const Vehicles: React.FC = () => {
    const [vehicles, setVehicles] = useState<AdaptedVehicle[]>([]);
    const [rawVehicles, setRawVehicles] = useState<Vehicle[]>([]);
    const authContext = useContext(AuthContext);

    useEffect(() => {
        const loadData = async () => {
            if (authContext?.isAuthenticated && authContext.token) {
                try {
                    const fetchedVehicles = await fetchVehicles(authContext.token);
                    setRawVehicles(fetchedVehicles);
                } catch (error) {
                    console.error('Error fetching vehicles:', error);
                }
            }
        };
        loadData();
    }, [authContext?.isAuthenticated, authContext?.token]);

    console.log(vehicles);

    const adaptedVehicles = rawVehicles.map(vehicle => ({
        id: vehicle.id,
        number: vehicle.number,
        liftingCapacity: vehicle.liftingCapacity.toString(),
        flightDistance: vehicle.flightDistance.toString(),
        stationNumber: vehicle.stationNumber,
        orderNumber: vehicle.order ? vehicle.order.number : '-',
        status: vehicle.status,
    }));

    console.log(adaptedVehicles);

    const navigate = useNavigate();

    return (
        <>
            <h1>Vehicles</h1>
            <Button variant="contained" onClick={() => navigate('/vehicles/create')}>
                Create
            </Button>
            <DataTable
                tableHeader={tableHeader}
                tableRows={adaptedVehicles}
                tableButton={{
                    buttonAction: (rowData) => {
                        navigate(`/vehicles/change/${rowData.id}`);
                    },
                    buttonLabel: 'Change'
                }}
            />
        </>
    );
};

export default Vehicles;