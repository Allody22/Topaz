import React from 'react';
import { Button, Image } from 'react-bootstrap';
import { useHistory } from 'react-router-dom';

const styles = {
    buttonContainer: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px' },
};

function ActionButton({ label, route, imageSrc, style }) {
    const history = useHistory();

    return (
        <div style={styles.buttonContainer}>
            <Button
                variant={"outline-dark"}
                className="mt-4 p-2 flex-grow-1"
                onClick={() => history.push(route)}
                style={{marginTop: '10px'}}
            >
                {label}
            </Button>
            {/* Добавляем атрибут loading="lazy" к изображению */}
            <Image src={imageSrc} fluid style={style} loading="lazy" />
        </div>
    );
}

export default ActionButton;
