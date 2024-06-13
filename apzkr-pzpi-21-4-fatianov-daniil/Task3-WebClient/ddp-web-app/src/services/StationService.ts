import axios from 'axios';

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

interface Vehicle {
    number: string;
    status: string;
}

const API_URL = 'http://localhost:8084/vehicle-station-service/station/';

const fetchStationsBasingForDropDown = async (token: string): Promise<{ label: string; value: string }[]> => {
    const response = await axios.get<Station[]>(`${API_URL}get-all`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    return response.data
        .filter(station => station.type === "BASING")
        .map(station => ({
            label: station.number,
            value: station.number
        }));
}

const fetchStationsBasing = async (token: string): Promise<Station[]> => {
    const response = await axios.get<Station[]>(`${API_URL}get-all`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
    return response.data.filter(station => station.type === "BASING");
}

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

const fetchStations = async (token: string): Promise<Station[]> => {
    const response = await axios.get<Station[]>(`${API_URL}get-all`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });
    return response.data;
}

interface StationAddRequest {
    description: string;
    latitude: number;
    longitude: number;
    altitude: number;
    type: string;
}

const addStation = async (stationData: StationAddRequest, token: string): Promise<void> => {
    const headers = {
        Authorization: `Bearer ${token}`
    };
    try {
        await axios.post(`${API_URL}add`, stationData, { headers });
        console.log('Station added successfully');
    } catch (error: any) {
        throw new Error(`Failed to add station: ${error.response?.data.message || error.message}`);
    }
};


interface StationChangeRequest {
    number: string;
    description: string;
    latitude: number;
    longitude: number;
    altitude: number;
    type: string;
}

const changeStation = async (stationData: StationChangeRequest, token: string): Promise<void> => {
    try {
        const response = await axios.put(`${API_URL}change-station`, stationData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        console.log('Station updated successfully:', response.data);
    } catch (error) {
        console.error('Failed to update station:', error);
        throw error;
    }
}

export { fetchStationsBasing };
export { changeStation };
export { addStation };
export { fetchStations };
export { fetchStationsBasingForDropDown };