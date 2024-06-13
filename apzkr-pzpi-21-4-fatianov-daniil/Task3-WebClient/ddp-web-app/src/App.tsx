import React from 'react';
import {Routes, Route} from 'react-router-dom';
import {AuthProvider} from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import {CssBaseline} from "@mui/material";
import Authentication from './components/Authentication'
import Header from './components/Header';
import Footer from './components/Footer';
import MainContent from "./components/MainContent";
import { BrowserRouter as Router } from 'react-router-dom';
import Users from './pages/Users';
import Vehicles from "./pages/Vehicles";
import VehicleCreate from "./pages/VehicleCreate";
import VehicleChange from "./pages/VehicleChange";
import Stations from "./pages/Stations";
import StationCreate from "./pages/StationCreate";
import StationChange from "./pages/StationChange";
import Orders from "./pages/Orders";
import OrderProcess from "./pages/OrderProcess";

const App: React.FC = () => {
    return (
        <Router>
            <AuthProvider>
                <CssBaseline/>
                <div className="App">
                    <Header/>
                    <MainContent>
                        <Routes>
                            <Route path="/login" element={<Authentication/>}/>
                            <Route element={<PrivateRoute/>}>
                                <Route path="/users" element={<Users />} />
                                <Route path="/vehicles" element={<Vehicles />} />
                                <Route path="/vehicles/create" element={<VehicleCreate />} />
                                <Route path="/vehicles/change/:id" element={<VehicleChange />} />
                                <Route path="/stations" element={<Stations />} />
                                <Route path="/stations/create" element={<StationCreate />} />
                                <Route path="/stations/change/:id" element={<StationChange />} />
                                <Route path="/orders" element={<Orders />} />
                                <Route path="/orders/process/:id" element={<OrderProcess />} />
                            </Route>
                        </Routes>
                    </MainContent>
                    <Footer/>
                </div>
            </AuthProvider>
        </Router>
    );
};

export default App;
