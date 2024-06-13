import React, {ReactNode} from 'react';
import styled from 'styled-components';

const MainContentDiv = styled.main`
    padding: 1em;
    min-height: calc(100vh - 176px);
`;

const Footer: React.FC<{ children: ReactNode }> = ({children}) => {
    return <MainContentDiv>{children}</MainContentDiv>;
};

export default Footer;