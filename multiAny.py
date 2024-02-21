###########################################################
# Pings any non-specific beacon that is closest in range  #
###########################################################

from flask import Flask
import asyncio
from bleak import BleakClient, BleakScanner

app = Flask(__name__)

LED_CONTROL_CHARACTERISTIC_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"  # Replace with the actual UUID

async def find_closest_beacon():
    devices = await BleakScanner.discover()
    closest_device = None
    max_rssi = -100  # Start with a very low RSSI value

    for device in devices:
        if device.rssi > max_rssi:
            max_rssi = device.rssi
            closest_device = device

    return closest_device

@app.route('/')
def trigger_led():
    closest_beacon = asyncio.run(find_closest_beacon())
    if closest_beacon:
        print(f"Closest beacon: {closest_beacon.address}")
        turn_on_led(closest_beacon.address)
        return "LED Triggered on closest beacon"
    else:
        return "No beacon found"

async def turn_on_led(beacon_address):
    async with BleakClient(beacon_address) as client:
        connected = await client.is_connected()
        if connected:
            await client.write_gatt_char(LED_CONTROL_CHARACTERISTIC_UUID, bytearray([0x01]))
            print("LED should now be on.")
        else:
            print("Failed to connect to the beacon.")

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)