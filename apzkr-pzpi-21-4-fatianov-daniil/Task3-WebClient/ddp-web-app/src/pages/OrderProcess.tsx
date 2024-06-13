import React, {useEffect, useState, useContext} from 'react';
import {useNavigate, useParams, useLocation} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import Dropdown from '../components/Dropdown';
import {Button, Box, TextField, Typography, Input} from '@mui/material';
import {fetchStationsBasing} from '../services/StationService';
import {processOrder} from '../services/OrderService';

interface Vehicle {
    number: string;
    status: string;
}

interface Station {
    id: number;
    number: string;
    description: string;
    latitude: number;
    longitude: number;
    altitude: number;
    type: string;
    vehicles: Vehicle[];
}

interface Item {
    name: string;
    description: string;
    quantity: number;
    weight: number;
    isFragile: boolean;
}

interface Order {
    id: string;
    userEmail: string;
    vehicleNumber: string;
    departureStationNumber: string;
    arrivalStationNumber: string;
    number: string;
    receiptCode: string;
    creationDate: string;
    status: string;
    items: Item[];
}

interface DropdownOption {
    label: string;
    value: string;
}

interface ProcessOrderRequest {
    id: string;
    vehicleNumber: string;
    departureStationNumber: string;
    items: Item[];
}

const OrderProcess: React.FC = () => {
    const {id} = useParams<{ id: string }>();
    const navigate = useNavigate();
    const location = useLocation();
    const orderFromLocation = location.state?.order as Order;
    const authContext = useContext(AuthContext);
    const token = authContext?.token;
    const [stations, setStations] = useState<Station[]>([]);
    const [selectedStation, setSelectedStation] = useState<string>('');
    const [selectedVehicle, setSelectedVehicle] = useState<string>('');
    const [vehicles, setVehicles] = useState<DropdownOption[]>([]);
    const [order, setOrder] = useState<Order | null>(orderFromLocation || null);

    useEffect(() => {
        if (token) {
            fetchStationsBasing(token).then(stations => {
                setStations(stations);
                if (stations.length > 0) {
                    setSelectedStation(stations[0].number);
                    updateVehicles(stations[0].vehicles);
                }
            }).catch(console.error);
        }
    }, [token]);

    console.log("Order: " + order?.id)

    useEffect(() => {
        const station = stations.find(s => s.number === selectedStation);
        if (station) {
            updateVehicles(station.vehicles);
        }
    }, [selectedStation, stations]);

    const updateVehicles = (vehicles: Vehicle[]) => {
        console.log(vehicles)
        const availableVehicles = vehicles
            .filter(v => v.status !== 'READY')
            .map(v => ({label: v.number, value: v.number}));
        setVehicles(availableVehicles);
        if (availableVehicles.length > 0) {
            setSelectedVehicle(availableVehicles[0].value);
        }
    };

    const handleSave = async () => {
        if (token && order && selectedStation && selectedVehicle) {
            const updatedOrder: ProcessOrderRequest = {
                id: order.id,
                vehicleNumber: selectedVehicle,
                departureStationNumber: selectedStation,
                items: order.items.map(item => ({...item}))
            };
            try {
                await processOrder(updatedOrder, token);
                navigate('/orders/');
            } catch (error) {
                console.error('Failed to process order:', error);
            }
        }
    };

    if (!order) return <div>Loading or order not found...</div>;

    return (
        <Box sx={{display: 'flex', flexDirection: 'column', width: 450, margin: 'auto'}}>
            <h1>Process Order</h1>
            <h3>Order: {order?.number}</h3>
            {order.items.map((item, index) => (
                <Box key={index} sx={{marginBottom: 2}}>
                    <Typography variant="h6">Item {index + 1}</Typography>
                    <Typography><strong>Item Name:</strong> {item.name}</Typography>
                    <Typography><strong>Description:</strong> {item.description}</Typography>
                    <Typography><strong>Quantity:</strong> {item.quantity}</Typography>
                    <TextField
                        fullWidth
                        label="Weight (KG)"
                        type="number"
                        inputProps={{step: "0.01"}}
                        variant="outlined"
                        value={item.weight === null ? '' : item.weight}
                        onChange={(e) => {
                            const value = e.target.value;
                            const weight = value === '' ? 0 : parseFloat(value);
                            if (!isNaN(weight)) {
                                const newItems = [...order.items];
                                newItems[index] = {...newItems[index], weight};
                                setOrder({...order, items: newItems});
                            }
                        }}
                        sx={{marginTop: 2}}
                    />
                </Box>
            ))}
            <Dropdown
                label="Departure Station"
                options={stations.map(s => ({label: s.number, value: s.number}))}
                value={selectedStation}
                onChange={setSelectedStation}
            />
            <Dropdown
                label="Vehicle Number"
                options={vehicles}
                value={selectedVehicle}
                onChange={setSelectedVehicle}
            />
            <Button onClick={handleSave} variant="contained" color="primary">
                Save
            </Button>
        </Box>
    );
};

export default OrderProcess;
