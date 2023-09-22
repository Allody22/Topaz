import '../css/CreatingOrder.css';
import {useState} from "react";

const InputField = ({ label, id, value, onChange, inputStyle }) => {
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

    return (
        <div className='input-container'>
            <label htmlFor={id} style={inputStyle}>{label}</label>
            <input
                type='text'
                className={`form-control ${hasValue ? 'has-value' : ''}`}
                id={id}
                value={value}
                placeholder={hasValue ? '' : 'Введите текст'}
                onChange={handleChange}
                onFocus={handleFocus}
                onBlur={handleBlur}
            />
        </div>
    );
};

export default InputField;