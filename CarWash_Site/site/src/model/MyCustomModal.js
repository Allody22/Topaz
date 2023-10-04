import React from 'react';
import {Button, Modal} from 'react-bootstrap';

const MyCustomModal = ({show, handleClose, title, children}) => {
    return (
        <Modal show={show}
               onHide={handleClose}
               dialogClassName="custom-modal-dialog">
            <Modal.Header closeButton>
                <Modal.Title>{title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {children}
            </Modal.Body>
            <Modal.Footer>
                <Button variant='secondary' onClick={handleClose}>
                    Закрыть
                </Button>
            </Modal.Footer>
        </Modal>
    );
}

export default MyCustomModal;
