import axios from 'axios';

const API_URL = 'http://localhost:8083/order-service/';

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

const fetchOrders = async (token: string): Promise<Order[]> => {
    const response = await axios.get<Order[]>(`${API_URL}get-all`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
    console.log("Orders response: ", response.data);
    const orders = response.data.map(order => ({
        ...order,
        items: order.items || []
    }));
    return orders;
};

interface ProcessOrderRequest {
    id: string;
    vehicleNumber: string;
    departureStationNumber: string;
    items: {
        name: string;
        description: string;
        quantity: number;
        weight: number;
        isFragile: boolean;
    }[];
}

interface ProcessOrderResponse {
    message: string;
}

const processOrder = async (orderData: ProcessOrderRequest, token: string): Promise<ProcessOrderResponse> => {
    try {
        const response = await axios.put(`${API_URL}process`, orderData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error: any) {
        throw new Error('Failed to process order: ' + (error.response?.data.message || error.message));
    }
};

export {processOrder};
export {fetchOrders};
