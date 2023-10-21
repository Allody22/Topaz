import React, {useState} from 'react';

const InputFieldNear = ({label, id, value, inputStyle, onChange, className}) => {
    const labelStyle = {
        fontWeight: 'bold',
        fontSize: '20px'
    };

    const [hasValue, setHasValue] = useState(value !== "");

    const handleChange = (e) => {
        setHasValue(e.target.value !== "");
        onChange(e.target.value);
    };

    const handleInputChange = (e) => {
        const regex = /^[0-9]+$/; // Этот регулярное выражение соответствует только целым положительным числам
        if (regex.test(e.target.value) || e.target.value === '') {
            handleChange(e);
        } else {
            // Если введенное значение не соответствует требованиям, оставляем значение неизменным
            e.preventDefault();
        }
    };


    return (
        <div className={`input-field-near ${className}`}>
            <label htmlFor={id} className='input-label-near' style={{marginRight: '10px', ...labelStyle}}>
                {label}
            </label>

            <input
                type='text'
                className={`form-control input-field ${hasValue ? 'has-value' : ''}`}
                id={id}
                value={value}
                placeholder={hasValue ? '' : 'Введите число'}
                onChange={handleInputChange}
                style={inputStyle}
            />
        </div>
    );
};

export default InputFieldNear;
