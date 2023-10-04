import React from 'react';

const InputFieldNear = ({label, id, value, inputStyle, onChange, className}) => {
    const labelStyle = {
        fontWeight: 'bold',
        fontSize: '20px'
    };

    return (
        <div className={`input-field-near ${className}`}>
            <label htmlFor={id} className='input-label-near' style={{marginRight: '10px', ...labelStyle}}>
                {label}
            </label>
            <input
                id={id}
                value={value}
                className={`form-control input-field-near ${className}`}
                style={inputStyle}
                onChange={(e) => onChange(e.target.value)}
            />
        </div>
    );
};

export default InputFieldNear;
