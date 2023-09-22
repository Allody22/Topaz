import React from 'react';

const InputFieldNear = ({
                            label,
                            id,
                            value,
                            inputStyle,
                            onChange
                        }) => {
    const labelStyle = {
        fontWeight: 'bold',
        fontSize: '20px'
    };

    return (
        <div style={{ display: 'flex', alignItems: 'center' }}>
            <label htmlFor={id} style={{ marginRight: '10px', ...labelStyle }}>{label}</label>
            <input
                id={id}
                value={value}
                style={inputStyle}
                onChange={e => onChange(e.target.value)}
            />
        </div>
    );
};

export default InputFieldNear;
