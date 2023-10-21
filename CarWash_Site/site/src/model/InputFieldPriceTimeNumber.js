import React, {useState} from 'react';


const InputFieldPriceTimeNumber = ({label, id, value, onChange, className, style, maxLength = 10}) => {
    const [hasValue, setHasValue] = useState(value !== "");
    const [isExceeded, setIsExceeded] = useState(false);

    const handleChange = (e) => {
        setHasValue(e.target.value !== "");
        setIsExceeded(e.target.value.length >= maxLength);  // Проверьте здесь
        onChange(e.target.value);
    };

    const handleNumericChange = (e) => {
        const regex = /^[0-9]+$/; // Этот регулярное выражение соответствует только целым положительным числам
        if (regex.test(e.target.value) || e.target.value === '') {
            handleChange(e);
        } else {
            // Если введенное значение не соответствует требованиям, оставляем значение неизменным
            e.preventDefault();
        }
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
                placeholder={hasValue ? '' : 'Введите число'}
                onChange={handleNumericChange}
                onFocus={handleFocus}
                onBlur={handleBlur}
                maxLength={maxLength}
                style={inputStyle}
            />
        </div>
    );
};

export default InputFieldPriceTimeNumber;
