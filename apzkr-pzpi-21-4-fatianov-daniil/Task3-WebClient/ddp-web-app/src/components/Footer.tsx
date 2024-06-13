import React from 'react';
import styled from 'styled-components';

const FooterContainer = styled.footer`
    background: #333;
    color: white;
    text-align: center;
    padding: 1em;
    width: 100%;
`;

const Footer: React.FC = () => {
    return (
        <FooterContainer>
            <p>&copy; 2024 My Company</p>
        </FooterContainer>
    );
};

export default Footer;