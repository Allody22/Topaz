import React from 'react';
import {InputNumber} from "rsuite";

const stylesForInput = {
    width: 190, marginBottom: 10, marginTop: 10
};
const ModalContent = ({filterType, handleItemChange, getValueByNameInSelectedItems, allServices}) => {
    return (
        allServices.filter(item => item.type.includes(filterType)).map(item => (
            <div key={item.name} style={{
                fontSize: '16px', borderBottom: '1px solid lightgray',
                paddingBottom: '10px', paddingTop: '10px'
            }}>
                <div style={{textAlign: 'center'}}>
                    <span>{item.name}</span>
                </div>
                <div style={{display: 'flex', justifyContent: 'space-between', marginTop: '7px'}}>
                    <div>
                        <span style={{color: "red"}}>Цены: </span>
                        <span>{`${item.priceFirstType} / ${item.priceSecondType} / ${item.priceThirdType}`}</span>
                    </div>
                    <div style={{marginLeft: 'auto'}}>
                        <span style={{color: "blue"}}>Время: </span>
                        <span>{`${item.timeFirstType} / ${item.timeSecondType} / ${item.timeThirdType}`}</span>
                    </div>
                </div>
                <div style={{display: 'flex', justifyContent: 'center'}}>
                    <InputNumber size="sm" placeholder="sm"
                                 style={Object.assign({}, stylesForInput, {margin: '0 auto'})}
                                 min={0}
                                 onChange={value => handleItemChange(item.name, value)}
                                 value={getValueByNameInSelectedItems(item.name) || 0}/>
                </div>
            </div>
        ))
    );
}

export default ModalContent;
