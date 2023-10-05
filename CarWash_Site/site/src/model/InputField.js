import React, {useState} from 'react';

const InputField = ({label, id, value, onChange, className, style}) => {
    const [hasValue, setHasValue] = useState(value !== "");

    const handleChange = (e) => {
        setHasValue(e.target.value !== "");
        onChange(e.target.value);
    };

    const handleFocus = () => {
        setHasValue(true);
    };

    const handleBlur = () => {
        setHasValue(value !== "");
    };

    const inputStyle = {
        fontSize: '17px',
        border: '1px solid #000',
        padding: '5px 10px',
        ...style,
    };

    return (
        <div className={`input-container ${className}`} style={style}>
            <label htmlFor={id} className='input-label'>
                {label}
            </label>
            <input
                type='text'
                className={`form-control input-field ${hasValue ? 'has-value' : ''}`}
                id={id}
                value={value}
                placeholder={hasValue ? '' : 'Введите текст'}
                onChange={handleChange}
                onFocus={handleFocus}
                onBlur={handleBlur}
                style={inputStyle} // Применяем стили к <input>
            />
        </div>
    );
};

export default InputField;
