import React from 'react';
import {Link} from 'react-router-dom';
import styled from 'styled-components';

const HeaderContainer = styled.header`
    background: #333;
    color: white;
    padding: 1em;
`;

const Nav = styled.nav`
    ul {
        list-style: none;
        padding: 0;
        display: flex;
        gap: 1em;
    }

    a {
        color: white;
        text-decoration: none;
    }
`;

const Header: React.FC = () => {
    return (
        <HeaderContainer>
            <Nav>
                <ul>
                    <li>
                        <Link to="/orders">Orders</Link>
                    </li>
                    <li>
                        <Link to="/users">Users</Link>
                    </li>
                    <li>
                        <Link to="/vehicles">Vehicles</Link>
                    </li>
                    <li>
                        <Link to="/stations">Stations</Link>
                    </li>
                    <li>
                        <Link to="/login">Login</Link>
                    </li>
                </ul>
            </Nav>
        </HeaderContainer>
    );
};

export default Header;
