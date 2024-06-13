import React from 'react';
import styled from 'styled-components';
import {Button} from '@mui/material';

const TableContainer = styled.div`
    margin: 1em 0;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    overflow: hidden;
`;

const StyledTable = styled.table`
    width: 100%;
    border-collapse: collapse;
`;

const TableHeader = styled.th`
    background-color: #333;
    color: white;
    padding: 1em;
    text-align: left;
    font-weight: bold;
`;

const TableRow = styled.tr`
    &:nth-child(even) {
        background-color: #f2f2f2;
    }
`;

const TableCell = styled.td`
    padding: 1em;
    border-bottom: 1px solid #ddd;
`;

const ActionButton = styled(Button)`
    display: block;
    margin: 0 auto;
`;

interface RowProps {
    [key: string]: string | number;
}

interface DataTableProps {
    tableHeader?: string[];
    tableRows: RowProps[];
    tableButton?: {
        buttonAction: (id: RowProps) => void;
        buttonLabel: string;
    };
}

const DataTable: React.FC<DataTableProps> = ({
                                                 tableHeader,
                                                 tableRows,
                                                 tableButton,
                                             }) => {
    const {buttonAction, buttonLabel} = tableButton ?? {};
    return (
        <TableContainer>
            <StyledTable>
                {tableHeader && (
                    <thead>
                    <TableRow>
                        {tableHeader.map((header, id) => (
                            <TableHeader key={id}>{header}</TableHeader>
                        ))}
                        {buttonAction && <TableHeader>Action</TableHeader>}
                    </TableRow>
                    </thead>
                )}
                <tbody>
                {tableRows.map((row, id) => (
                    <TableRow key={id}>
                        {Object.keys(row).map((cellName, i) => (
                            <TableCell key={`${id}-${i}`}>{row[cellName]}</TableCell>
                        ))}
                        {buttonAction && (
                            <TableCell>
                                <ActionButton
                                    onClick={() => buttonAction(row)}
                                    variant="outlined"
                                >
                                    {buttonLabel}
                                </ActionButton>
                            </TableCell>
                        )}
                    </TableRow>
                ))}
                </tbody>
            </StyledTable>
        </TableContainer>
    );
};

export default DataTable;