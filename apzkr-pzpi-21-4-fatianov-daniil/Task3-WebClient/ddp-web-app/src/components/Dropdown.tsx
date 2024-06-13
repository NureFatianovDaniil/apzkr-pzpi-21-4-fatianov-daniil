import * as React from 'react';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, {SelectChangeEvent} from '@mui/material/Select';

interface DropdownProps {
    options: { label: string; value: string }[];
    value: string;
    label: string;
    onChange: (value: string) => void;
    fullWidth?: boolean;
}

const Dropdown: React.FC<DropdownProps> = ({
                                               options,
                                               value: defaultValue,
                                               onChange,
                                               label,
                                               fullWidth = false,
                                           }) => {
    const [value, setValue] = React.useState(defaultValue ?? '');

    const handleChange = (event: SelectChangeEvent<string>) => {
        const selectedValue = event.target.value;
        setValue(selectedValue);
        onChange(selectedValue);
    };

    return (
        <FormControl fullWidth={fullWidth} sx={{m: 1, minWidth: 120}}>
            <InputLabel id={`label-${label}`}>{label}</InputLabel>
            <Select
                labelId={`label-${label}`}
                id={`select-${label}`}
                value={value}
                label={label}
                onChange={handleChange}
                sx={{'& .MuiSelect-select': {padding: '10px'}}}
            >
                {options.map((option, index) => (
                    <MenuItem key={index} value={option.value}>
                        {option.label}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
    );
};

export default Dropdown;