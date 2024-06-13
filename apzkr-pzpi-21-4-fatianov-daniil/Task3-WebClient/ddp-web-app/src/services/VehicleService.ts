import axios from 'axios';

const API_URL = 'http://localhost:8084/vehicle-station-service/vehicle/';

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
    }
    status: string;
}

const fetchVehicles = async (token: string): Promise<Vehicle[]> => {
    const response = await axios.get(`${API_URL}get-all`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
    return response.data;
}

interface VehicleAddRequest {
    liftingCapacity: number;
    flightDistance: number;
    stationNumber: string;
}

interface VehicleAddResponse {
    message: string;
}

const addVehicle = async (vehicleData: VehicleAddRequest, token: string): Promise<VehicleAddResponse> => {
    try {
        const response = await axios.post<VehicleAddResponse>(`${API_URL}add`, vehicleData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error: any) {
        throw new Error('Failed to add vehicle: ' + (error.response?.data.message || error.message));
    }
};

interface VehicleChangeRequest {
    id: number;
    number: string;
    liftingCapacity: number;
    flightDistance: number;
}

const changeVehicle = async (vehicleData: VehicleChangeRequest, token: string): Promise<void> => {
    await axios.put(`${API_URL}change`, vehicleData, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
}


export {fetchVehicles};
export {addVehicle};
export {changeVehicle};