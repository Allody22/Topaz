import React, {useState} from 'react';

const InputField = ({label, id, value, onChange, className, style, maxLength = 200}) => {
    const [hasValue, setHasValue] = useState(value !== "");
    const [isExceeded, setIsExceeded] = useState(false);

    const handleChange = (e) => {
        setHasValue(e.target.value !== "");
        setIsExceeded(e.target.value.length >= maxLength);  // Проверьте здесь
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
        border: isExceeded ? '1px solid red' : '1px solid #000',  // Измените цвет границы здесь
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
                style={inputStyle}
                maxLength={maxLength}
            />
        </div>
    );
};

export default InputField;
