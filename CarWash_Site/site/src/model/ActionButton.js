import React from 'react';
import { Button, Image } from 'react-bootstrap';
import { useHistory } from 'react-router-dom';

const styles = {
    buttonContainer: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px' },
    image: { width: '100px', height: '60px', marginLeft: '15px', marginTop: '20px' },
    verySmallImage: { width: '85px', height: '60px', marginLeft: '30px', marginTop: '20px' },
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
            <Image src={imageSrc} fluid style={style} />
        </div>
    );
}

export default ActionButton;
