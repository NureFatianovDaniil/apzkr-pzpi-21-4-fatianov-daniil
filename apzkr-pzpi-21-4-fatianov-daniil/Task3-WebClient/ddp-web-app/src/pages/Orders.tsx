import React, {useEffect, useState, useContext} from 'react';
import {AuthContext} from '../context/AuthContext';
import {fetchOrders} from '../services/OrderService';
import DataTable from '../components/DataTable';
import {useNavigate} from 'react-router-dom';

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
    items: { name: string; description: string; quantity: number; weight: number; isFragile: boolean }[];
}

interface AdaptedOrder {
    id: string;
    userEmail: string;
    vehicleNumber: string;
    departureStationNumber: string;
    arrivalStationNumber: string;
    number: string;
    receiptCode: string;
    creationDate: string;
    status: string;
    itemCount: number;
}

const tableHeader: string[] = [
    'Id',
    'User Email',
    'Vehicle Number',
    'Departure Station',
    'Arrival Station',
    'Order Number',
    'Receipt Code',
    'Creation Date',
    'Status',
    'Item Count',
    'Action'
];

const Orders: React.FC = () => {
    const [orders, setOrders] = useState<Order[]>([]);
    const authContext = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        const loadData = async () => {
            if (authContext?.isAuthenticated && authContext.token) {
                try {
                    const fetchedOrders = await fetchOrders(authContext.token);
                    setOrders(fetchedOrders);
                } catch (error) {
                    console.error('Error fetching orders:', error);
                }
            }
        };
        loadData();
    }, [authContext?.isAuthenticated, authContext?.token]);

    console.log(orders)
    const adaptedOrders = orders.map(order => ({
        id: order.id,
        userEmail: order.userEmail,
        vehicleNumber: order.vehicleNumber,
        departureStationNumber: order.departureStationNumber,
        arrivalStationNumber: order.arrivalStationNumber,
        number: order.number,
        receiptCode: order.receiptCode,
        creationDate: order.creationDate,
        status: order.status,
        itemCount: order.items ? order.items.length : 0
    }));

    return (
        <>
            <h1>Orders</h1>
            <DataTable
                tableHeader={tableHeader}
                tableRows={adaptedOrders}
                tableButton={{
                    buttonAction: (rowData) => {
                        const order = orders.find(o => o.id === rowData.id);
                        navigate(`/orders/process/${rowData.id}`, {state: {order}});
                    },
                    buttonLabel: 'Change'
                }}
            />
        </>
    );
};

export default Orders;
